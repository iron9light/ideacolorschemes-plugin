/*
 * Copyright 2012 IL <iron9light AT gmali DOT com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ideacolorschemes.ideacolor

import reflect.BeanProperty
import com.ideacolorschemes.commons.entities.ColorSchemeId
import java.util.Date
import com.ideacolorschemes.commons.WithTimestamp
import com.intellij.openapi.editor.colors.{EditorColorsScheme, EditorColorsManager}
import util.{IdeaUtil, Loggable}
import com.intellij.openapi.project.{ProjectManager, Project}
import annotation.tailrec

/**
 * @author il
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

trait SchemeBookManager extends Loggable with IdeaUtil with IdeaSchemeNameUtil {
  private[this] def bookSetting: BookSetting = service[IdeaSettings]

  def currentBook = {
    Option(bookSetting.currentBook)
  }

  def currentBook_=(value: Option[String]) {
    bookSetting.currentBook = value.getOrElse(null)
  }

  val editorColorsManager = EditorColorsManager.getInstance()
  
  private val colorSchemeManager = service[ColorSchemeManager]

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

  private[this] val updateLock = new AnyRef

  def update()(implicit project: Project = ProjectManager.getInstance.getDefaultProject) = updateLock.synchronized {
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
    SiteServices.schemeBookNames match {
      case None => Nil
      case Some(nameAndTimestamps) =>
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
  }

  private def applyUpdateModifies(modifies: List[ModifyBook]) {
    (for {
      UpdateBook(bookName, timestamp) <- modifies
    } yield {
      () => {
        SiteServices.schemeBook(bookName) match {
          case Some(schemeIds) =>
            if (schemeIds.forall(checkSchemeId)) {
              val newBook = SchemeBook(bookName, schemeIds, timestamp)
              put(newBook)
            }
            // else, keep the old one
          case _ =>
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

  @tailrec
  private def checkSchemeId(schemeId: ColorSchemeId): Boolean = {
    colorSchemeManager.get(schemeId) match {
      case None =>
        false
      case Some(scheme) =>
        scheme.dependencies.flatMap(_.find{_.target == schemeId.target}) match {
          case None =>
            true
          case Some(dependencyId) =>
            checkSchemeId(dependencyId)
        }
    }
  }

  def loadAllBookScheme()(implicit project: Project = ProjectManager.getInstance.getDefaultProject) {
    ideaRun {
      for {
        book <- getAll if book.schemeIds.forall(checkSchemeId)
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
      val currentIdeaScheme = editorColorsManager.getGlobalScheme
      val targetSchemeName = if (isBook(currentIdeaScheme)) {
        EditorColorsScheme.DEFAULT_SCHEME_NAME
      } else {
        currentIdeaScheme.getName
      }
      val ideaSchemes = editorColorsManager.getAllSchemes.toList
      editorColorsManager.removeAllSchemes()
      for {
        ideaScheme <- ideaSchemes if !isBook(ideaScheme)
      } {
        editorColorsManager.addColorsScheme(ideaScheme)
        if (ideaScheme.getName == targetSchemeName) {
          editorColorsManager.setGlobalScheme(ideaScheme)
        }
      }
    }
  }

  private def book2ideaScheme(book: SchemeBook) = new IdeaColorScheme(ideaSchemeName(book.name), book.schemeIds) with CachedEditorColorsScheme with EditableIdeaColorScheme
}

trait IdeaSchemeNameUtil {
  private[this] final val capital = 0x7f.toChar.toString
  
  implicit def convertEditorColorsScheme(scheme: EditorColorsScheme) = new {
    def name = scheme.getName.substring(capital.length)
  }
  
  def isBook(scheme: EditorColorsScheme) = scheme.getName.startsWith(capital)
  
  def ideaSchemeName(name: String) = capital + name
}
