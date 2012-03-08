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

import collection.JavaConversions._
import java.awt.Color
import com.intellij.application.options.colors.ColorSettingsUtil
import com.ideacolorschemes.commons.entities._
import com.ideacolorschemes.commons.Implicits._
import com.intellij.openapi.editor.colors.{ColorKey, EditorColorsManager, EditorColorsScheme}
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
    val defaultScheme = EditorColorsManager.getInstance().getScheme(EditorColorsScheme.DEFAULT_SCHEME_NAME)
    def getDefaultColor(key: ColorKey) = {
      Option(key.getDefaultColor) match {
        case None =>
          Option(defaultScheme.getColor(key))
        case color => color
      }
    }
    (for {
      colorDescriptor <- page.getColorDescriptors
      color <- getDefaultColor(colorDescriptor.getKey)
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
    val consoleFontName = Option(scheme.getConsoleFontName)
    val consoleFontSize = Some(scheme.getConsoleFontSize)
    val consoleLineSpacing = Some(scheme.getConsoleLineSpacing)
    FontSetting(editorFontName, editorFontSize, lineSpacing, quickDocFontSize, consoleFontName, consoleFontSize, consoleLineSpacing)
  }

  def color2int(color: Color) = color.getRGB & 0xFFFFFF
}