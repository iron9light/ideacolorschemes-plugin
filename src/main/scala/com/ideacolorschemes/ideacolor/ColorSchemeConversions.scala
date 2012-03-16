package com.ideacolorschemes.ideacolor

import java.awt.Color
import com.intellij.openapi.editor.markup.{EffectType => JEffectType, TextAttributes}
import com.ideacolorschemes.commons.entities._
import com.ideacolorschemes.commons.Implicits._
import com.intellij.openapi.options.FontSize
import annotation.tailrec

/**
 * @author il
 */
trait ColorSchemeConversions {
  implicit def toColor(i: Option[Int]) = i.filter(_ >= 0).map(new Color(_)).getOrElse(null)

  implicit def toEffectType(e: Option[EffectType.Value]) = e.map(x => {
    JEffectType.values()(x.id)
  }).getOrElse(null)

  implicit def toFontTypeInt(f: Option[FontType.Value]) = f.map(_.id).getOrElse(0)

  implicit def toTextAttributes(x: Option[TextAttributesObject]) = x match {
    case Some(o) if o != null =>
      val textAttributes = new TextAttributes(o.foregroundColor, o.backgroundColor, o.effectColor, o.effectType, o.fontType)
      textAttributes.setErrorStripeColor(o.errorStripeColor)
      textAttributes
    case _ => null
  }

  implicit def toFontSize(size: Int): FontSize = {
    @tailrec
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

  implicit def toInt(color: Color) = {
    if (color == null)
      -1
    else
      color.getRGB & 0xFFFFFF
  }

  implicit def toTextAttributesObject(textAttributes: TextAttributes) = {
    if (textAttributes == null) {
      null
    } else {
      val foregroundColor = Option(textAttributes.getForegroundColor).map(toInt)
      val backgroundColor = Option(textAttributes.getBackgroundColor).map(toInt)
      val effectColor = Option(textAttributes.getEffectColor).map(toInt)
      val effectType = effectColor.map {
        _ => EffectType(textAttributes.getEffectType.ordinal())
      }
      val fontType = textAttributes.getFontType match {
        case 0 => FontType.PLAIN
        case 1 => FontType.BOLD
        case 2 => FontType.ITALIC
        case 3 => FontType.BOLD_ITALIC
        case _ => FontType.PLAIN
      }
      val errorStripeColor = Option(textAttributes.getErrorStripeColor).map(toInt)

      TextAttributesObject(foregroundColor, backgroundColor, effectColor, effectType, fontType, errorStripeColor)
    }
  }
}
