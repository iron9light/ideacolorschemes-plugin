package com.ideacolorschemes.ideacolor

import com.intellij.openapi.editor.colors.{ColorKey, TextAttributesKey}
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import com.ideacolorschemes.commons.entities.{FontSetting, Version, ColorSchemeId, ColorScheme}
import com.intellij.openapi.options.FontSize
import org.jdom.Element
import util.JDomHelper


/**
 * @author il
 */
trait EditableIdeaColorScheme extends IdeaColorScheme {
  private[this] var changedScheme = {
    val initScheme = initChangedScheme
    initScheme.copy(id = initScheme.id.copy(name = name), dependencies = Some(colorSchemeIds))
  }
  
  def initChangedScheme = ColorScheme(ColorSchemeId("", name, Version.NoVersion, ""), Some(colorSchemeIds))
  
  def change = {
    val scheme = if (changedScheme.fontSetting == Some(FontSetting())) {
      changedScheme.copy(fontSetting = None)
    } else {
      changedScheme
    }
    
    if (scheme.fontSetting.isEmpty && scheme.attributes.isEmpty && scheme.colors.isEmpty) {
      None
    } else {
      Some(scheme)
    }
  }

  override def getAttributes(key: TextAttributesKey) = {
    changedScheme.attributes.get(key.getExternalName) match {
      case None => super.getAttributes(key)
      case x => x
    }
  }

  override def setAttributes(key: TextAttributesKey, attributes: TextAttributes) {
    if (attributes != getAttributes(key)) {
      val newAttributes = if (attributes != super.getAttributes(key)) {
        changedScheme.attributes + (key.getExternalName -> toTextAttributesObject(attributes))
      } else {
        changedScheme.attributes - key.getExternalName
      }

      changedScheme = changedScheme.copy(attributes = newAttributes)
    }
  }

  override def getColor(key: ColorKey) = {
    changedScheme.colors.get(key.getExternalName) match {
      case None => super.getColor(key)
      case x => x
    }
  }

  override def setColor(key: ColorKey, color: Color) {
    if (color != getColor(key)) {
      val newColors = if (color != super.getColor(key)) {
        changedScheme.colors + (key.getExternalName -> toInt(color))
      } else {
        changedScheme.colors - key.getExternalName
      }
      changedScheme = changedScheme.copy(colors = newColors)
    }
  }

  override def getEditorFontSize = changedScheme.fontSetting.flatMap(_.editorFontSize).getOrElse(super.getEditorFontSize)

  override def setEditorFontSize(fontSize: Int) {
    if (fontSize != getEditorFontSize) {
      val editorFontSize = if (fontSize != super.getEditorFontSize) {
        Some(fontSize)
      } else {
        None
      }

      val fontSetting = changedScheme.fontSetting.orElse(Some(FontSetting()).map(_.copy(editorFontSize = editorFontSize)))

      changedScheme = changedScheme.copy(fontSetting = fontSetting)

      initFonts()
    }
  }

  override def getQuickDocFontSize = changedScheme.fontSetting.flatMap(_.quickDocFontSize).map(toFontSize).getOrElse(super.getQuickDocFontSize)

  override def setQuickDocFontSize(fontSize: FontSize) {
    if (fontSize != getQuickDocFontSize) {
      val quickDocFontSize = if (fontSize != super.getQuickDocFontSize) {
        Some(fontSize.getSize)
      } else {
        None
      }

      val fontSetting = changedScheme.fontSetting.orElse(Some(FontSetting()).map(_.copy(quickDocFontSize = quickDocFontSize)))

      changedScheme = changedScheme.copy(fontSetting = fontSetting)
    }
  }

  override def getEditorFontName = changedScheme.fontSetting.flatMap(_.editorFontName).getOrElse(super.getEditorFontName)

  override def setEditorFontName(fontName: String) {
    if (fontName != getEditorFontName) {
      val editorFontName = if (fontName != super.getEditorFontName) {
        Some(fontName)
      } else {
        None
      }

      val fontSetting = changedScheme.fontSetting.orElse(Some(FontSetting()).map(_.copy(editorFontName = editorFontName)))

      changedScheme = changedScheme.copy(fontSetting = fontSetting)

      initFonts()
    }
  }

  override def getLineSpacing = changedScheme.fontSetting.flatMap(_.lineSpacing).map(fixLineSpacing).getOrElse(super.getLineSpacing)

  override def setLineSpacing(lineSpacing: Float) {
    if (lineSpacing != getLineSpacing) {
      val editorLineSpacing = if (lineSpacing != super.getLineSpacing) {
        Some(lineSpacing)
      } else {
        None
      }

      val fontSetting = changedScheme.fontSetting.orElse(Some(FontSetting()).map(_.copy(lineSpacing = editorLineSpacing)))

      changedScheme = changedScheme.copy(fontSetting = fontSetting)
    }
  }

  override def getConsoleFontName = changedScheme.fontSetting.flatMap(_.consoleFontName).getOrElse(super.getConsoleFontName)

  override def setConsoleFontName(fontName: String) {
    if (fontName != getConsoleFontName) {
      val consoleFontName = if (fontName != super.getConsoleFontName) {
        Some(fontName)
      } else {
        None
      }

      val fontSetting = changedScheme.fontSetting.orElse(Some(FontSetting()).map(_.copy(consoleFontName = consoleFontName)))

      changedScheme = changedScheme.copy(fontSetting = fontSetting)

      initFonts()
    }
  }

  override def getConsoleFontSize = changedScheme.fontSetting.flatMap(_.consoleFontSize).getOrElse(super.getConsoleFontSize)

  override def setConsoleFontSize(fontSize: Int) {
    if (fontSize != getConsoleFontSize) {
      val consoleFontSize = if (fontSize != super.getConsoleFontSize) {
        Some(fontSize)
      } else {
        None
      }

      val fontSetting = changedScheme.fontSetting.orElse(Some(FontSetting()).map(_.copy(consoleFontSize = consoleFontSize)))

      changedScheme = changedScheme.copy(fontSetting = fontSetting)

      initFonts()
    }
  }

  override def getConsoleLineSpacing = changedScheme.fontSetting.flatMap(_.consoleLineSpacing).map(fixLineSpacing).getOrElse(super.getConsoleLineSpacing)

  override def setConsoleLineSpacing(lineSpacing: Float) {
    if (lineSpacing != getConsoleLineSpacing) {
      val consoleLineSpacing = if (lineSpacing != super.getConsoleLineSpacing) {
        Some(lineSpacing)
      } else {
        None
      }

      val fontSetting = changedScheme.fontSetting.orElse(Some(FontSetting()).map(_.copy(consoleLineSpacing = consoleLineSpacing)))

      changedScheme = changedScheme.copy(fontSetting = fontSetting)
    }
  }

  override def scanIdDeepFold[K, V](func: ColorScheme => Map[K, V])(implicit ids: List[ColorSchemeId], manager: ColorSchemeManager) = {
    super.scanIdDeepFold(func)(ids, manager) ++ func(changedScheme)
  }

  private def changedFontSettingGet[T](func: FontSetting => Option[T]): Option[T] = changedScheme.fontSetting.flatMap(func).orElse(super.fontSettingGet(func))

  override def writeExternal(parentNode: Element) {
    JDomHelper.build(toXml(new {
      def apply[T](func: FontSetting => Option[T]): Option[T] = changedFontSettingGet(func)
    }), parentNode)
  }
}
