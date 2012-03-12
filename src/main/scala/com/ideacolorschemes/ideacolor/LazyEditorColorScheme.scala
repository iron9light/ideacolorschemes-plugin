package com.ideacolorschemes.ideacolor

import com.intellij.openapi.editor.colors.impl.ReadOnlyColorsScheme
import com.intellij.openapi.editor.markup.TextAttributes
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

  lazy val getDefaultBackground = underlying.getDefaultBackground

  lazy val getDefaultForeground = underlying.getDefaultForeground

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

  lazy val getEditorFontSize = underlying.getEditorFontSize

  def setEditorFontSize(fontSize: Int) {}

  lazy val getQuickDocFontSize = underlying.getQuickDocFontSize

  def setQuickDocFontSize(fontSize: FontSize) {}

  lazy val getEditorFontName = underlying.getEditorFontName

  def setEditorFontName(fontName: String) {}

  def getFont(key: EditorFontType) = underlying.getFont(key)

  def setFont(key: EditorFontType, font: Font) {}

  lazy val getLineSpacing = underlying.getLineSpacing

  def setLineSpacing(lineSpacing: Float) {}

  lazy val getConsoleFontName = underlying.getConsoleFontName

  def setConsoleFontName(fontName: String) {}

  lazy val getConsoleFontSize = underlying.getConsoleFontSize

  def setConsoleFontSize(fontSize: Int) {}

  lazy val getConsoleLineSpacing = underlying.getConsoleLineSpacing

  def setConsoleLineSpacing(lineSpacing: Float) {}

  def readExternal(element: Element) {
    throw new UnsupportedOperationException
  }

  def writeExternal(element: Element) {
    underlying.writeExternal(element)
  }

  lazy val getName = underlying.getName

  override def clone(): AnyRef = {
    classOf[EditorColorsScheme].getMethod("clone").invoke(underlying)
  }
}
