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

import com.ideacolorschemes.commons.entities._
import com.ideacolorschemes.commons.Implicits._
import scala.xml.{XML, NodeSeq, Node}
import org.jdom.Element
import com.intellij.openapi.editor.colors.{EditorColorsManager, EditorColorsScheme}
import java.lang.reflect.Constructor

/**
 * @author il
 * @version 11/8/11 11:26 PM
 */

object ColorSchemeParser {
  private val DEFAULT_SCHEME_NAME = EditorColorsScheme.DEFAULT_SCHEME_NAME

  private val EDITOR_FONT_NAME = "EDITOR_FONT_NAME"
  private val CONSOLE_FONT_NAME = "CONSOLE_FONT_NAME"
  private val SCHEME_NAME = "SCHEME_NAME"
  private val SCHEME_ELEMENT = "scheme"
  private val NAME_ATTR = "name"
  private val VERSION_ATTR = "version"
  private val DEFAULT_SCHEME_ATTR = "default_scheme"
  private val PARENT_SCHEME_ATTR = "parent_scheme"
  private val OPTION_ELEMENT = "option"
  private val COLORS_ELEMENT = "colors"
  private val ATTRIBUTES_ELEMENT = "attributes"
  private val VALUE_ELEMENT = "value"
  private val BACKGROUND_COLOR_NAME = "BACKGROUND"
  private val LINE_SPACING = "LINE_SPACING"
  private val CONSOLE_LINE_SPACING = "CONSOLE_LINE_SPACING"
  private val EDITOR_FONT_SIZE = "EDITOR_FONT_SIZE"
  private val CONSOLE_FONT_SIZE = "CONSOLE_FONT_SIZE"
  private val EDITOR_QUICK_JAVADOC_FONT_SIZE = "EDITOR_QUICK_DOC_FONT_SIZE"

  def parse(editorColorsScheme: EditorColorsScheme): Option[ColorScheme] = {
    val element = new Element(SCHEME_ELEMENT)
    editorColorsScheme.writeExternal(element)
    val source = new org.jdom.transform.JDOMSource(element)
    val node = XML.load(source.getInputSource)
    val colorScheme = parse(node).map {
      case (scheme, None) => scheme
      case (scheme, Some(parentSchemeName)) =>
        Option(EditorColorsManager.getInstance().getScheme(parentSchemeName)).flatMap(parse(_)) match {
          case None => scheme
          case Some(parentScheme) =>
            scheme.copy(
              fontSetting = mergeFontSetting(scheme.fontSetting, parentScheme.fontSetting),
              colors = parentScheme.colors ++ scheme.colors,
              attributes = parentScheme.attributes ++ scheme.attributes
            )
        }
    }

    val isDefaultScheme = EditorColorsManager.getInstance.isDefaultScheme(editorColorsScheme)

    colorScheme.map(_.copy(isDefaultScheme = isDefaultScheme))
  }

  private lazy val constructor = classOf[FontSetting].getDeclaredConstructors.head.asInstanceOf[Constructor[FontSetting]]

  private def mergeFontSetting(fontSetting1: Option[FontSetting], fontSetting2: Option[FontSetting]): Option[FontSetting] = {
    fontSetting1 match {
      case None => fontSetting2
      case Some(fontSetting) =>
        val arguments = (for (i <- 0 to fontSetting.productArity) yield {
          fontSetting.productElement(i) match {
            case None => fontSetting2.flatMap(_.productElement(i).asInstanceOf[Option[_]])
            case x => x
          }
        }).toArray
        constructor.newInstance(arguments)
    }
  }

  def parse(node: Node): Option[(ColorScheme, Option[String])] = {
    if (node.label == SCHEME_ELEMENT)
      Some(parseScheme(node))
    else
      (node \\ SCHEME_ELEMENT).headOption.map(parseScheme)
  }

  def parseScheme(node: Node) = {
    val attrMap = node.attributes.asAttrMap
    val name = attrMap(NAME_ATTR)
    val version = attrMap.get(VERSION_ATTR).map(Version(_)).getOrElse(Version.NoVersion)
    val id = ColorSchemeId(UserManager.userId, name, version, "")
    val isDefaultScheme = attrMap.get(DEFAULT_SCHEME_ATTR).map(_.toBoolean).getOrElse(false)
    val parentSchemeName = if (isDefaultScheme) {
      None
    } else {
      attrMap.get(PARENT_SCHEME_ATTR) match {
        case Some(DEFAULT_SCHEME_NAME) => None
        case parent => parent
      }
    }

    val fontSetting = parseFontSetting(node \ OPTION_ELEMENT)

    val colors = (node \ COLORS_ELEMENT).headOption.map(parseColors).getOrElse(Map[String, Int]())

    val attributes = (node \ ATTRIBUTES_ELEMENT).headOption.map(parseAttributes).getOrElse(Map[String, TextAttributesObject]())

    (ColorScheme(id, None, isDefaultScheme, fontSetting, colors, attributes), parentSchemeName)
  }

  def parseFontSetting(nodes: NodeSeq): FontSetting = {
    val processes: Seq[FontSetting => FontSetting] = for {
      node <- nodes
      name <- node.attribute(NAME_ATTR).map(_.toString())
      value <- node.attribute(VALUE_ELEMENT).map(_.toString())
    } yield {
      name match {
        case LINE_SPACING =>
          setting: FontSetting => setting.copy(lineSpacing = Some(value.toFloat))
        case EDITOR_FONT_SIZE =>
          setting: FontSetting => setting.copy(editorFontSize = Some(value.toInt))
        case EDITOR_FONT_NAME =>
          setting: FontSetting => setting.copy(editorFontName = Some(value))
        case CONSOLE_LINE_SPACING =>
          setting: FontSetting => setting.copy(consoleLineSpacing = Some(value.toFloat))
        case CONSOLE_FONT_SIZE =>
          setting: FontSetting => setting.copy(consoleFontSize = Some(value.toInt))
        case CONSOLE_FONT_NAME =>
          setting: FontSetting => setting.copy(consoleFontName = Some(value))
        case EDITOR_QUICK_JAVADOC_FONT_SIZE =>
          setting: FontSetting => setting.copy(quickDocFontSize = Some(value.toInt))
        case _ => setting: FontSetting => setting
      }
    }

    processes.foldLeft(FontSetting.Empty)((setting, func) => func(setting))
  }

  def parseColors(node: Node): Map[String, Int] = {
    (for {
      child <- node \ OPTION_ELEMENT
      name <- child.attribute(NAME_ATTR).map(_.toString())
      value <- child.attribute(VALUE_ELEMENT).map(_.toString())
      color <- tryReadColorInt(value)
    } yield (name, color)).toMap
  }

  def tryReadColorInt(value: String): Option[Int] = try {
    Some(Integer.parseInt(value, 16))
  } catch {
    case _: NumberFormatException => None
  }

  def parseAttributes(node: Node): Map[String, TextAttributesObject] = {
    (for {
      child <- node \ OPTION_ELEMENT
      name <- child.attribute(NAME_ATTR).map(_.toString())
      value <- (child \ VALUE_ELEMENT).headOption
      attribute <- tryReadAttribute(value)
    } yield (name, attribute)).toMap
  }

  private val FOREGROUND = "FOREGROUND"
  private val BACKGROUND = "BACKGROUND"
  private val FONT_TYPE = "FONT_TYPE"
  private val EFFECT_COLOR = "EFFECT_COLOR"
  private val EFFECT_TYPE = "EFFECT_TYPE"
  private val ERROR_STRIPE_COLOR = "ERROR_STRIPE_COLOR"

  def tryReadAttribute(node: Node): Option[TextAttributesObject] = {
    val processes: Seq[TextAttributesObject => TextAttributesObject] = for {
      child <- node \ OPTION_ELEMENT
      name <- child.attribute(NAME_ATTR).map(_.toString())
      value <- child.attribute(VALUE_ELEMENT).map(_.toString())
    } yield name match {
        case FOREGROUND =>
          attribute: TextAttributesObject => {
            tryReadColorInt(value).map(x => attribute.copy(foregroundColor = Some(x))).getOrElse(attribute)
          }
        case BACKGROUND =>
          attribute: TextAttributesObject => {
            tryReadColorInt(value).map(x => attribute.copy(backgroundColor = Some(x))).getOrElse(attribute)
          }
        case FONT_TYPE =>
          attribute: TextAttributesObject => {
            try {
              val x = value.toInt match {
                case i if (i < 0 || i > 3) => 0 // this may change in the next version
                case i => i
              }
              attribute.copy(fontType = Some(FontType(x)))
            } catch {
              case _: NumberFormatException => attribute
            }
          }
        case EFFECT_COLOR =>
          attribute: TextAttributesObject => {
            tryReadColorInt(value).map(x => attribute.copy(effectColor = Some(x))).getOrElse(attribute)
          }
        case EFFECT_TYPE =>
          val EFFECT_BORDER = 0
          val EFFECT_LINE = 1
          val EFFECT_WAVE = 2
          val EFFECT_STRIKEOUT = 3
          val EFFECT_BOLD_LINE = 4
          val EFFECT_BOLD_DOTTED_LINE = 5
          attribute: TextAttributesObject => {
            try {
              val x = value.toInt match {
                case EFFECT_BORDER =>
                  Some(EffectType.BOXED)
                case EFFECT_BOLD_LINE =>
                  Some(EffectType.BOLD_LINE_UNDERSCORE)
                case EFFECT_LINE =>
                  Some(EffectType.LINE_UNDERSCORE)
                case EFFECT_STRIKEOUT =>
                  Some(EffectType.STRIKEOUT)
                case EFFECT_WAVE =>
                  Some(EffectType.WAVE_UNDERSCORE)
                case EFFECT_BOLD_DOTTED_LINE =>
                  Some(EffectType.BOLD_DOTTED_LINE)
                case _ => None
              }
              x.map(_ => attribute.copy(effectType = x)).getOrElse(attribute)
            } catch {
              case _: NumberFormatException => attribute
            }
          }
        case ERROR_STRIPE_COLOR =>
          attribute: TextAttributesObject => {
            tryReadColorInt(value).map(x => attribute.copy(errorStripeColor = Some(x))).getOrElse(attribute)
          }
        case _ => attribute: TextAttributesObject => attribute
      }

    processes.foldLeft(TextAttributesObject.Empty)((attributeValue, func) => func(attributeValue)) match {
      case TextAttributesObject.Empty => None
      case x => Some(x)
    }
  }
}