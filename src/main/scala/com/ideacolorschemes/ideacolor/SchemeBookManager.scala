package com.ideacolorschemes.ideacolor

import net.liftweb.json._
import com.intellij.openapi.editor.colors.EditorColorsManager

/**
 * @author il
 * @version 12/20/11 9:39 AM
 */

class SchemeBookManager {
  def books = {
    SiteServices.schemeBookNames match {
      case Some(JArray(list: List[JObject])) =>
        for {
          book <- list
        } yield {
          val bookName = book.values("name").toString
          val ids = SiteServices.schemeBook(bookName)
          (bookName, ids)
        }
      case _ =>
        Nil
    }
  }
  
  def addBooks() {
    val editorColorsManager = EditorColorsManager.getInstance()
    for {
      (bookName, ids) <- books if !ids.isEmpty
    } {
      editorColorsManager.addColorsScheme(new IdeaColorScheme(bookName, ids))
    }
  }
}
