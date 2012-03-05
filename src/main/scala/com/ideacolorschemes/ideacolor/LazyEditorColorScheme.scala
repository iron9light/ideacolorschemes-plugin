package com.ideacolorschemes.ideacolor

import com.intellij.openapi.editor.colors.impl.ReadOnlyColorsScheme
import com.intellij.openapi.editor.markup.TextAttributes
import reflect.BeanProperty
import com.intellij.openapi.options.FontSize
import com.intellij.openapi.editor.colors.{EditorFontType, ColorKey, TextAttributesKey, EditorColorsScheme}
import java.awt.{Font, Color}
import org.jdom.Element

/**
 * @author il
 */
class LazyEditorColorScheme(private[this] val underlying: EditorColorsScheme) extends EditorColorsScheme with ReadOnlyColorsScheme {
  import collection.mutable.Map

  def setName(name: String) {}

  private val attributesMap = Map[TextAttributesKey, TextAttributes]()

  def getAttributes(key: TextAttributesKey) = {
    attributesMap.get(key) match {
      case Some(value) => value
      case None =>
        val value = underlying.getAttributes(key)
        attributesMap += (key -> value)
        value
    }
  }

  def setAttributes(key: TextAttributesKey, attributes: TextAttributes) {}

  @BeanProperty
  lazy val defaultBackground = underlying.getDefaultBackground

  @BeanProperty
  lazy val defaultForeground = underlying.getDefaultForeground

  private val colorMap = Map[ColorKey, Color]()

  def getColor(key: ColorKey) = {
    colorMap.get(key) match {
      case Some(value) => value
      case None =>
        val value = underlying.getColor(key)
        colorMap += (key -> value)
        value
    }
  }

  def setColor(key: ColorKey, color: Color) {}

  @BeanProperty
  lazy val editorFontSize = underlying.getEditorFontSize

  def setEditorFontSize(fontSize: Int) {}

  @BeanProperty
  lazy val quickDocFontSize = underlying.getQuickDocFontSize

  def setQuickDocFontSize(fontSize: FontSize) {}

  @BeanProperty
  lazy val editorFontName = underlying.getEditorFontName

  def setEditorFontName(fontName: String) {}
  
  private val fontMap = Map[EditorFontType, Font]()

  def getFont(key: EditorFontType) = {
    fontMap.get(key) match {
      case Some(value) => value
      case None =>
        val value = underlying.getFont(key)
        fontMap += (key -> value)
        value
    }
  }

  def setFont(key: EditorFontType, font: Font) {}

  @BeanProperty
  lazy val lineSpacing = underlying.getLineSpacing

  def setLineSpacing(lineSpacing: Float) {}

  @BeanProperty
  lazy val consoleFontName = underlying.getConsoleFontName

  def setConsoleFontName(fontName: String) {}

  @BeanProperty
  lazy val consoleFontSize = underlying.getConsoleFontSize

  def setConsoleFontSize(fontSize: Int) {}

  @BeanProperty
  lazy val consoleLineSpacing = underlying.getConsoleLineSpacing

  def setConsoleLineSpacing(lineSpacing: Float) {}

  def readExternal(element: Element) {
    throw new UnsupportedOperationException
  }

  def writeExternal(element: Element) {
    underlying.writeExternal(element)
  }

  private lazy val theName = underlying.getName

  def getName = theName
}
