package com.ideacolorschemes.ideacolor

import reflect.BeanProperty
import com.ideacolorschemes.commons.entities.ColorSchemeId
import java.util.Date
import com.ideacolorschemes.commons.WithTimestamp
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager}
import com.intellij.openapi.project.ProjectManager
import util.Loggable

/**
 * @author il
 * @version 12/20/11 9:39 AM
 */

trait BookSetting {
  @BeanProperty
  var currentBook: String = _
}

case class SchemeBook(name: String, schemeIds: List[ColorSchemeId], timestamp: Date) extends WithTimestamp

sealed abstract class ModifyBook {
  def name: String
}

case class UpdateBook(name: String, timestamp: Date) extends ModifyBook

case class DeleteBook(name: String) extends ModifyBook

trait SchemeBookManager extends UserManager.Sub with Loggable {
  private[this] def bookSetting: BookSetting = ServiceManager.getService(classOf[IdeaSettings])
  
  def currentBook = {
    Option(bookSetting.currentBook).flatMap(get)
  }
  
  def currentBook_=(value: Option[String]) {
    bookSetting.currentBook = value.getOrElse(null)
  }
  
  val editorColorsManager = EditorColorsManager.getInstance()
  
  def get(name: String): Option[SchemeBook]
  
  def getAll: List[SchemeBook]
  
  def remove(name: String)
  
  def removeAll()
  
  def put(schemeBook: SchemeBook)

  def contains(name: String): Boolean

  def notify(pub: UserManager.Pub, event: String) {
    removeAll()

    ProgressManager.getInstance().run(new Backgroundable(ProjectManager.getInstance.getDefaultProject, "Update color schemes", false) {
      def run(indicator: ProgressIndicator) {
        initUpdate()
      }
    })
  }
  
  def update() = {
    val nameAndTimestamps = SiteServices.schemeBookNames
    val allBooks = getAll
    val modifies = allBooks.flatMap {
      book => {
        nameAndTimestamps.find(_._1 == book.name) match {
          case None =>
            Some(DeleteBook(book.name))
          case Some((bookName, timestamp)) if timestamp.after(book.timestamp) =>
            Some(UpdateBook(bookName, timestamp))
          case _ =>
            None
        }
      }
    } ::: nameAndTimestamps.flatMap {
      case (name, timestamp) if allBooks.forall(_.name != name)=>
        Some(UpdateBook(name, timestamp))
      case _ => None
    }
    
    modifies
  }
  
  def initUpdate() {
    val modifies = update()
    
    (for {
      UpdateBook(bookName, timestamp) <- modifies
    } yield {
      () => {
        val schemeIds = SiteServices.schemeBook(bookName)
        if (!schemeIds.isEmpty) {
          val newBook = SchemeBook(bookName, schemeIds, timestamp)
          put(newBook)
        } else {
          // keep the old one
        }
      }
    }).par.map(_()).toList
    
    modifies.foreach {
      case DeleteBook(name) =>
        remove(name)
        for {
          currentBookName <- currentBook if currentBookName == name
        } {
          currentBook = None
        }
      case _ =>
    }
    
    var currentBookUnset = true
    for {
      book <- getAll
    } {
      val ideaColorScheme = book2ideaScheme(book)
      editorColorsManager.addColorsScheme(ideaColorScheme)
      if (currentBookUnset && currentBook.exists(_.name == book.name)) {
        editorColorsManager.setGlobalScheme(ideaColorScheme)
        currentBookUnset = false
      }
    }
    
    if (currentBookUnset) {
      currentBook = None
    }
  }
  
  def book2ideaScheme(book: SchemeBook) = new IdeaColorScheme(book.name, book.schemeIds)
}
