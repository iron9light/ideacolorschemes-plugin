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

import org.jdom.Element
import java.lang.String
import com.intellij.openapi.options.FontSize
import java.awt.{Color, Font}
import com.intellij.openapi.editor.colors._
import com.intellij.openapi.editor.markup.TextAttributes


/**
 * @author il
 * @version 12/22/11 3:02 PM
 */

object Sandbox {
  def run() {
//    val manager = EditorColorsManager.getInstance()
//    for{
//     scheme <- manager.getAllSchemes
//    } {
//      println("%s[%s] - %s" format (scheme, manager.isDefaultScheme(scheme), scheme.getClass))
//    }
//    SchemeBookManager.addBooks()
//    val editorColorsManager = EditorColorsManager.getInstance()
//    editorColorsManager.addColorsScheme(new MockEditorColorScheme("mock"))
  }
}

class MockEditorColorScheme(name: String) extends EditorColorsScheme {
  val defaultEditorColorsScheme = EditorColorsManager.getInstance.getScheme(EditorColorsManager.DEFAULT_SCHEME_NAME)

  def getName = name

  def readExternal(element: Element) {}

  def writeExternal(element: Element) {}

  def setName(name: String) {}

  def getAttributes(key: TextAttributesKey) = defaultEditorColorsScheme.getAttributes(key)

  def setAttributes(key: TextAttributesKey, attributes: TextAttributes) {}

  def getDefaultBackground = defaultEditorColorsScheme.getDefaultBackground

  def getDefaultForeground = defaultEditorColorsScheme.getDefaultForeground

  def getColor(key: ColorKey) = defaultEditorColorsScheme.getColor(key)

  def setColor(key: ColorKey, color: Color) {}

  def getEditorFontSize = defaultEditorColorsScheme.getEditorFontSize

  def setEditorFontSize(fontSize: Int) {}

  def getQuickDocFontSize = defaultEditorColorsScheme.getQuickDocFontSize

  def setQuickDocFontSize(fontSize: FontSize) {}

  def getEditorFontName = defaultEditorColorsScheme.getEditorFontName

  def setEditorFontName(fontName: String) {}

  def getFont(key: EditorFontType) = defaultEditorColorsScheme.getFont(key)

  def setFont(key: EditorFontType, font: Font) {}

  def getLineSpacing = defaultEditorColorsScheme.getLineSpacing

  def setLineSpacing(lineSpacing: Float) {}

  def getConsoleFontName = defaultEditorColorsScheme.getConsoleFontName

  def setConsoleFontName(fontName: String) {}

  def getConsoleFontSize = defaultEditorColorsScheme.getConsoleFontSize

  def setConsoleFontSize(fontSize: Int) {}

  def getConsoleLineSpacing = defaultEditorColorsScheme.getConsoleLineSpacing

  def setConsoleLineSpacing(lineSpacing: Float) {}
}