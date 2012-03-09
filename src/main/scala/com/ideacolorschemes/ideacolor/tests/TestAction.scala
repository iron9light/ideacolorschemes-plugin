package com.ideacolorschemes.ideacolor
package tests

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import com.intellij.openapi.options.colors.{ColorSettingsPage, ColorSettingsPages}
import com.intellij.openapi.editor.colors.{EditorColorsScheme, EditorColorsManager}
import com.ideacolorschemes.commons.entities.ColorScheme
import util.{IdeaUtil, Loggable}
import org.jdom.Element
import scala.xml.XML


/**
 * @author il
 */
class TestAction extends AnAction with Loggable with IdeaSchemeNameUtil {
  val editorColorsManager = EditorColorsManager.getInstance

  def actionPerformed(e: AnActionEvent) {
    test1()
  }
  
  def test1() {
    val generalPage = ColorSettingsPages.getInstance.getRegisteredPages.find(_.getDisplayName == "General").get
    val schemeX = editorColorsManager.getScheme("Solarized Dark coffee")
    //    val schemeY = editorColorsManager.getScheme(ideaSchemeName("Solarized Dark coffee"))
    val scheme0 = ColorSchemeParser.parse(schemeX).get
    val schemeY = new TestColorsScheme("x", scheme0)
    compareEditorColorsScheme(schemeX, schemeY, generalPage)
  }
  
  def test2() {
    val schemeX = editorColorsManager.getScheme("Solarized Dark coffee")
    val element = new Element("xx")
    schemeX.writeExternal(element)
    val source = new org.jdom.transform.JDOMSource(element)
    val node = XML.load(source.getInputSource)
    logger.warn(node.toString())
  }
  
  def compareEditorColorsScheme(schemeX: EditorColorsScheme, schemeY: EditorColorsScheme, colorSettingsPage: ColorSettingsPage) {
    for {
      colorDescriptor <- colorSettingsPage.getColorDescriptors
    } {
      val key = colorDescriptor.getKey
      val colorX = schemeX.getColor(key)
      val colorY = schemeY.getColor(key)
      test(colorX, colorY, "%s (%s)".format(colorDescriptor.getDisplayName, key))
    }
    
    for {
      attributesDescriptor <- colorSettingsPage.getAttributeDescriptors
    } {
      val key = attributesDescriptor.getKey
      val attributeX = schemeX.getAttributes(key)
      val attributeY = schemeY.getAttributes(key)
      test(attributeX, attributeY, "%s (%s)".format(attributesDescriptor.getDisplayName, key))
    }
  }
  
  def test[T](x: T, y: T, msg: => String) = {
    if (x != y) {
      logger.warn("[test failed] %s\nx:%s\ny:%s".format(msg, x, y))
      false
    } else {
      true
    }
  }
}

class TestColorsScheme(name: String, scheme: ColorScheme) extends EditorColorsScheme with IdeaUtil {
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
  
  private val defaultEditorColorsScheme = EditorColorsManager.getInstance.getScheme(EditorColorsManager.DEFAULT_SCHEME_NAME)

  implicit protected def colorSchemeManager: ColorSchemeManager = service[ColorSchemeManager]

  implicit private def toColor(i: Option[Int]) = i.filter(_ >= 0).map(new Color(_)).getOrElse(null)

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
  
  def setName(name: String) {}

  def getAttributes(key: TextAttributesKey): TextAttributes = key match {
    case HighlighterColors.TEXT if highlighterTextAttributes.isDefined => highlighterTextAttributes.get
    case _ =>
      scheme.attributes.get(key.getExternalName) match {
        case None => defaultEditorColorsScheme.getAttributes(key)
        case x => x
      }
  }

  def setAttributes(key: TextAttributesKey, attributes: TextAttributes) {}

  def getDefaultBackground = null

  def getDefaultForeground = null

  def getColor(key: ColorKey) = scheme.colors.get(key.getExternalName) match {
    case None => defaultEditorColorsScheme.getColor(key)
    case x => x
  }

  def setColor(key: ColorKey, color: Color) {}

  def getEditorFontSize = 0

  def setEditorFontSize(fontSize: Int) {}

  def getQuickDocFontSize = null

  def setQuickDocFontSize(fontSize: FontSize) {}

  def getEditorFontName = ""

  def setEditorFontName(fontName: String) {}

  def getFont(key: EditorFontType) = null

  def setFont(key: EditorFontType, font: Font) {}

  def getLineSpacing = 0.0f

  def setLineSpacing(lineSpacing: Float) {}

  def getConsoleFontName = ""

  def setConsoleFontName(fontName: String) {}

  def getConsoleFontSize = 0

  def setConsoleFontSize(fontSize: Int) {}

  def getConsoleLineSpacing = 0.0f

  def setConsoleLineSpacing(lineSpacing: Float) {}

  def readExternal(element: Element) {}

  def writeExternal(element: Element) {}

  def getName = name

  private val highlighterTextAttributes = fixDeprecatedBackgroundColor

  // This setting has been deprecated to usages of HighlighterColors.TEXT attributes
  private def fixDeprecatedBackgroundColor: Option[TextAttributes] = {
    scheme.colors.get(IdeaColorScheme.BACKGROUND_COLOR_NAME).map {
      deprecatedBackgroundColor => {
        scheme.attributes.get(HighlighterColors.TEXT.getExternalName) match {
          case None =>
            new TextAttributes(Color.black, Some(deprecatedBackgroundColor), null, com.intellij.openapi.editor.markup.EffectType.BOXED, Font.PLAIN)
          case Some(attributes) =>
            Some(attributes.copy(backgroundColor = Some(deprecatedBackgroundColor)))
        }
      }
    }
  }
}
