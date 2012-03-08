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
import java.awt.{Color, Font}
import annotation.tailrec
import com.intellij.openapi.editor.markup.{EffectType => JEffectType, TextAttributes}
import com.ideacolorschemes.commons.entities._
import com.intellij.openapi.editor.colors._
import com.intellij.openapi.util.WriteExternalException
import com.intellij.openapi.editor.HighlighterColors
import com.ideacolorschemes.commons.Implicits._
import com.intellij.openapi.options.FontSize
import java.util.EnumMap
import util.{Loggable, IdeaUtil}

/**
 * @author il
 */

trait ColorSchemeUtil {
  final def deepGet[T](func: ColorScheme => Option[T], id: ColorSchemeId, wideSearch: Boolean = false)(implicit manager: ColorSchemeManager): Option[T] = {
    manager.get(id) match {
      case None => None
      case Some(colorScheme) =>
        func(colorScheme) match {
          case None =>
            //            colorScheme.parentScheme match {
            //              case None => None
            //              case Some(parentId) => deepGet(func, parentId)(manager)
            //            }
            val dependencies: List[ColorSchemeId] = colorScheme.dependencies
            for (dependency <- dependencies if wideSearch || dependency.target == id.target) {
              val result = deepGet(func, dependency, false)
              if (result.isDefined)
                return result
            }
            None
          case result => result
        }
    }
  }

  @tailrec
  final def scanIdGet[T](func: ColorSchemeId => Option[T])(implicit ids: List[ColorSchemeId]): Option[T] = {
    ids match {
      case id :: tail =>
        func(id) match {
          case None => scanIdGet(func)(tail)
          case result => result
        }
      case Nil =>
        None
    }
  }

  def scanIdDeepGet[T](func: ColorScheme => Option[T])(implicit ids: List[ColorSchemeId], manager: ColorSchemeManager) = scanIdGet(deepGet(func, _)(manager))

  def scanGet[T](func: ColorScheme => Option[T])(implicit ids: List[ColorSchemeId], manager: ColorSchemeManager) = scanIdGet(manager.get(_).map(func).getOrElse(None))
}

class IdeaColorScheme(val name: String, implicit val colorSchemeIds: List[ColorSchemeId]) extends EditorColorsScheme with ColorSchemeUtil with IdeaUtil with Loggable {

  private val defaultEditorColorsScheme = EditorColorsManager.getInstance.getScheme(EditorColorsManager.DEFAULT_SCHEME_NAME)

  implicit protected def colorSchemeManager: ColorSchemeManager = service[ColorSchemeManager]

  implicit private def toColor(i: Option[Int]) = i.map(new Color(_)).getOrElse(null)

  implicit private def toEffectType(e: Option[EffectType.Value]) = e.map(x => {
    JEffectType.values()(x.id)
  }).getOrElse(null)

  implicit private def toFontTypeInt(f: Option[FontType.Value]) = f.map(_.id).getOrElse(0)

  implicit private def toTextAttributes(x: Option[TextAttributesObject]) = x.map {
    o => {
      val textAttributes = new TextAttributes(o.foregroundColor, o.backgroundColor, o.effectColor, o.effectType, o.fontType)
      textAttributes.setErrorStripeColor(o.errorStripeColor)
      textAttributes
    }
  }.getOrElse(null)

  def getColor(colorKey: ColorKey): Color = getColor(colorKey.getExternalName) match {
    case None => defaultEditorColorsScheme.getColor(colorKey)
    case x => x
  }

  def getColor(key: String): Option[Int] = scanIdDeepGet(_.colors.get(key))

  def setName(p1: String) {
    // do nothing
    logger.info("setName")
  }

  def getAttributes(textAttributesKey: TextAttributesKey): TextAttributes = getAttributes(textAttributesKey.getExternalName) match {
    case None => defaultEditorColorsScheme.getAttributes(textAttributesKey)
    case x => x
  }

  def getAttributes(key: String) = scanIdDeepGet(_.attributes.get(key))

  def setAttributes(p1: TextAttributesKey, p2: TextAttributes) {
    // do nothing
    logger.info("setAttributes")
  }

  def getDefaultBackground = Option(getAttributes(HighlighterColors.TEXT).getBackgroundColor).getOrElse(defaultEditorColorsScheme.getDefaultBackground)

  def getDefaultForeground = Option(getAttributes(HighlighterColors.TEXT).getForegroundColor).getOrElse(defaultEditorColorsScheme.getDefaultForeground)

  def setColor(p1: ColorKey, p2: Color) {
    // do nothing
    logger.info("setColor")
  }

  def fontSettingGet[T](func: FontSetting => Option[T]) = scanIdDeepGet(_.fontSetting match {
    case None => None
    case Some(fontSetting) => func(fontSetting)
  })

  def fontSettingGetOrElse[T](func: FontSetting => Option[T], default: => T) = fontSettingGet(func).getOrElse(default)

  def getEditorFontSize = fontSettingGetOrElse(_.editorFontSize, defaultEditorColorsScheme.getEditorFontSize)

  def setEditorFontSize(p1: Int) {
    // do nothing
  }

  def getQuickDocFontSize = fontSettingGetOrElse(_.quickDocFontSize.map {
    size => {
      def round(fontSize: FontSize): FontSize = {
        val larger = fontSize.larger()
        if (fontSize == larger)
          fontSize
        else if (larger.getSize < size)
          round(larger)
        else {
          val delta1 = size - fontSize.getSize
          val delta2 = larger.getSize - size
          if (delta1.abs < delta2.abs) fontSize else larger
        }
      }

      round(FontSize.values()(0))
    }
  }, defaultEditorColorsScheme.getQuickDocFontSize)

  def setQuickDocFontSize(p1: FontSize) {
    // do nothing
  }

  def getEditorFontName = fontSettingGetOrElse(_.editorFontName, defaultEditorColorsScheme.getEditorFontName)

  def setEditorFontName(p1: String) {
    // do nothing
  }

  def getFont(key: EditorFontType) = myFonts.get(key)

  def setFont(key: EditorFontType, font: Font) {
    myFonts.put(key, font)
  }

  def getLineSpacing = fontSettingGetOrElse(_.lineSpacing, defaultEditorColorsScheme.getLineSpacing)

  def setLineSpacing(p1: Float) {
    // do nothing
  }

  def readExternal(p1: Element) {
    // do nothing
  }

  def writeExternal(p1: Element) {
    logger.info("writeExternal")
    throw new WriteExternalException
  }

  def getName = name

  def getConsoleFontName = fontSettingGetOrElse(_.consoleFontName, getEditorFontName)

  def setConsoleFontName(name: String) {
    // do nothing
  }

  def getConsoleFontSize = fontSettingGetOrElse(_.consoleFontSize, getEditorFontSize)

  def setConsoleFontSize(x: Int) {
    // do nothing
  }

  def getConsoleLineSpacing = fontSettingGetOrElse(_.consoleLineSpacing, getLineSpacing)

  def setConsoleLineSpacing(x: Float) {
    // do nothing
  }

  //  override def clone(): AnyRef = {
  //    logger.info("clone")
  //    println("clone")
  //    this
  //  }

  private val myFonts = new EnumMap[EditorFontType, Font](classOf[EditorFontType])
  private var myFallbackFontName: Option[String] = None

  private def initFonts() {
    var editorFontName = getEditorFontName
    val editorFontSize = getEditorFontSize

    var plainFont = new Font(editorFontName, Font.PLAIN, editorFontSize)
    if (plainFont.getFamily == "Dialog" && editorFontName != "Dialog") {
      editorFontName = defaultEditorColorsScheme.getEditorFontName
      myFallbackFontName = Some(editorFontName)
      plainFont = new Font(editorFontName, Font.PLAIN, editorFontSize)
    } else {
      myFallbackFontName = None
    }

    val boldFont: Font = new Font(editorFontName, Font.BOLD, editorFontSize)
    val italicFont: Font = new Font(editorFontName, Font.ITALIC, editorFontSize)
    val boldItalicFont: Font = new Font(editorFontName, Font.BOLD + Font.ITALIC, editorFontSize)

    myFonts.put(EditorFontType.PLAIN, plainFont)
    myFonts.put(EditorFontType.BOLD, boldFont)
    myFonts.put(EditorFontType.ITALIC, italicFont)
    myFonts.put(EditorFontType.BOLD_ITALIC, boldItalicFont)

    val consoleFontName: String = getConsoleFontName
    val consoleFontSize: Int = getConsoleFontSize

    val consolePlainFont: Font = new Font(consoleFontName, Font.PLAIN, consoleFontSize)
    val consoleBoldFont: Font = new Font(consoleFontName, Font.BOLD, consoleFontSize)
    val consoleItalicFont: Font = new Font(consoleFontName, Font.ITALIC, consoleFontSize)
    val consoleBoldItalicFont: Font = new Font(consoleFontName, Font.BOLD + Font.ITALIC, consoleFontSize)

    myFonts.put(EditorFontType.CONSOLE_PLAIN, consolePlainFont)
    myFonts.put(EditorFontType.CONSOLE_BOLD, consoleBoldFont)
    myFonts.put(EditorFontType.CONSOLE_ITALIC, consoleItalicFont)
    myFonts.put(EditorFontType.CONSOLE_BOLD_ITALIC, consoleBoldItalicFont)
  }

  initFonts()
}