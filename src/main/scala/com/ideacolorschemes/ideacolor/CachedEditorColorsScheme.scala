package com.ideacolorschemes.ideacolor

import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.colors.{ColorKey, TextAttributesKey, EditorColorsScheme}
import java.awt.Color


trait CachedEditorColorsScheme extends EditorColorsScheme {

  import collection.mutable.Map

  private[this] val attributesMap = Map[TextAttributesKey, TextAttributes]()

  override abstract def getAttributes(key: TextAttributesKey) = {
    attributesMap.get(key) match {
      case Some(value) => value
      case None =>
        val value = super.getAttributes(key)
        attributesMap += (key -> value)
        value
    }
  }

  private[this] val colorMap = Map[ColorKey, Color]()

  override abstract def getColor(key: ColorKey) = {
    colorMap.get(key) match {
      case Some(value) => value
      case None =>
        val value = super.getColor(key)
        colorMap += (key -> value)
        value
    }
  }

  private[this] lazy val _getEditorFontSize = super.getEditorFontSize

  override abstract def getEditorFontSize = _getEditorFontSize

  private[this] lazy val _getQuickDocFontSize = super.getQuickDocFontSize

  override abstract def getQuickDocFontSize = _getQuickDocFontSize

  private[this] lazy val _getEditorFontName = super.getEditorFontName

  override abstract def getEditorFontName = _getEditorFontName

  private[this] lazy val _getLineSpacing = super.getLineSpacing

  override abstract def getLineSpacing = _getLineSpacing

  private[this] lazy val _getConsoleFontName = super.getConsoleFontName

  override abstract def getConsoleFontName = _getConsoleFontName

  private[this] lazy val _getConsoleFontSize = super.getConsoleFontSize

  override abstract def getConsoleFontSize = _getConsoleFontSize

  private[this] lazy val _getConsoleLineSpacing = super.getConsoleLineSpacing

  override abstract def getConsoleLineSpacing = _getConsoleLineSpacing
}
