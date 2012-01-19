package com.ideacolorschemes.ideacolor

import collection.JavaConversions._
import java.awt.Color
import com.intellij.application.options.colors.ColorSettingsUtil
import com.ideacolorschemes.commons.entities._
import com.ideacolorschemes.commons.Implicits._
import com.intellij.openapi.editor.colors.{EditorColorsManager, EditorColorsScheme}
import com.intellij.openapi.options.colors.ColorSettingsPage

object DefaultColorSchemeExtractor {
  def extract(page: ColorSettingsPage) = {
    val id = extractId(page)

    val colors = extractColors(page)
    val attributes = extractAttributes(page)
    val colorScheme = ColorScheme(id, None, true, None, colors, attributes)
    if (colorScheme.isGeneral) {
      val defaultScheme = EditorColorsManager.getInstance().getScheme(EditorColorsScheme.DEFAULT_SCHEME_NAME)
      val fontSetting = extractFontSetting(defaultScheme)
      colorScheme.copy(fontSetting = fontSetting)
    } else {
      colorScheme
    }
  }

  def extractId(page: ColorSettingsPage) = {
    val (author, versionStringOption) = IdAndVersionUtil.idAndVersion(page)
    val version = extractVersion(versionStringOption)
    val name = EditorColorsScheme.DEFAULT_SCHEME_NAME
    val target = page.getDisplayName
    ColorSchemeId(author, name, version, target)
  }

  def extractVersion(x: Option[String]) = x match {
    case None => Version.NoVersion
    case Some(versionString) =>
      try {
        Version(versionString)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          Version.NoVersion
      }
  }

  def extractColors(page: ColorSettingsPage) = {
    (for {
      colorDescriptor <- page.getColorDescriptors
      color <- Option(colorDescriptor.getKey.getDefaultColor)
    } yield (colorDescriptor.getKey.getExternalName, color2int(color))).toMap
  }

  def extractAttributes(page: ColorSettingsPage) = {
    (for {
      descriptor <- ColorSettingsUtil.getAllAttributeDescriptors(page)
      textAttributes <- Option(descriptor.getKey.getDefaultAttributes)
      if (!textAttributes.isEmpty)
    } yield {
      val key = descriptor.getKey.getExternalName

      val foregroundColor = Option(textAttributes.getForegroundColor).map(color2int)
      val backgroundColor = Option(textAttributes.getBackgroundColor).map(color2int)
      val effectColor = Option(textAttributes.getEffectColor).map(color2int)
      val effectType = effectColor.map {
        _ => EffectType(textAttributes.getEffectType.ordinal())
      }
      val fontType = textAttributes.getFontType match {
        case 0 => FontType.PLAIN
        case 1 => FontType.ITALIC
        case 2 => FontType.BOLD
        case 3 => FontType.BOLD_ITALIC
        case _ => FontType.PLAIN
      }
      val errorStripeColor = Option(textAttributes.getErrorStripeColor).map(color2int)
      (key, TextAttributesObject(foregroundColor, backgroundColor, effectColor, effectType, fontType, errorStripeColor))
    }).toMap
  }

  def extractFontSetting(scheme: EditorColorsScheme) = {
    val editorFontName = Option(scheme.getEditorFontName)
    val editorFontSize = Some(scheme.getEditorFontSize)
    val lineSpacing = Some(scheme.getLineSpacing)
    val quickDocFontSize = Option(scheme.getQuickDocFontSize).map(_.getSize)
    FontSetting(editorFontName, editorFontSize, lineSpacing, quickDocFontSize)
  }

  def color2int(color: Color) = color.getRGB & 0xFFFFFF
}