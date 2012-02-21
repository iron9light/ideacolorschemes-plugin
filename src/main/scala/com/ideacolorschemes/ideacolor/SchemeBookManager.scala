package com.ideacolorschemes.ideacolor

import reflect.BeanProperty
import com.ideacolorschemes.commons.entities.ColorSchemeId
import java.util.Date
import com.ideacolorschemes.commons.WithTimestamp
import com.intellij.openapi.editor.colors.{EditorColorsScheme, EditorColorsManager}
import util.{IdeaUtil, Loggable}
import com.intellij.openapi.project.{ProjectManager, Project}

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

trait SchemeBookManager extends Loggable with IdeaUtil {
  private[this] def bookSetting: BookSetting = service[IdeaSettings]

  def currentBook = {
    Option(bookSetting.currentBook)
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

  def reset()(implicit project: Project = ProjectManager.getInstance.getDefaultProject) {
    unloadAllBookScheme()

    removeAll()

    currentBook = None
  }

  def initUpdate()(implicit project: Project = ProjectManager.getInstance.getDefaultProject) {
    val modifies = getUpdateModifies

    applyUpdateModifies(modifies)

    loadAllBookScheme()
  }

  def update()(implicit project: Project = ProjectManager.getInstance.getDefaultProject) = {
    val modifies = getUpdateModifies

    if (modifies.isEmpty) {
      false
    } else {
      applyUpdateModifies(modifies)

      if (modifies.exists(_.isInstanceOf[DeleteBook])) {
        unloadAllBookScheme()
      }

      loadAllBookScheme()
      true
    }
  }

  private def getUpdateModifies = {
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
      case (name, timestamp) if allBooks.forall(_.name != name) =>
        Some(UpdateBook(name, timestamp))
      case _ => None
    }

    modifies
  }

  private def applyUpdateModifies(modifies: List[ModifyBook]) {
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
  }

  private def loadAllBookScheme()(implicit project: Project) {
    ideaRun {
      for {
        book <- getAll
      } {
        val ideaColorScheme = book2ideaScheme(book)
        editorColorsManager.addColorsScheme(ideaColorScheme)
        if (currentBook.exists(_ == book.name)) {
          editorColorsManager.setGlobalScheme(ideaColorScheme)
        }
      }
    }
  }

  private def unloadAllBookScheme()(implicit project: Project) {
    ideaRun {
      val currentSchemeName = editorColorsManager.getGlobalScheme.getName
      val targetSchemeName = if (contains(currentSchemeName)) {
        EditorColorsScheme.DEFAULT_SCHEME_NAME
      } else {
        currentSchemeName
      }
      val ideaSchemes = editorColorsManager.getAllSchemes.toList
      editorColorsManager.removeAllSchemes()
      for {
        ideaScheme <- ideaSchemes if !contains(ideaScheme.getName)
      } {
        editorColorsManager.addColorsScheme(ideaScheme)
        if (ideaScheme.getName == targetSchemeName) {
          editorColorsManager.setGlobalScheme(ideaScheme)
        }
      }
    }
  }

  private def book2ideaScheme(book: SchemeBook) = new IdeaColorScheme(book.name, book.schemeIds)
}
