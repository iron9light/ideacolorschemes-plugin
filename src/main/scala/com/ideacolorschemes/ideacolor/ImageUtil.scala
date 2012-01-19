package com.ideacolorschemes.ideacolor

import javax.swing.Icon
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import com.ideacolorschemes.commons.Binary
import java.io.{IOException, ByteArrayOutputStream}

object ImageUtil {
  val IMAGE_FORMAT = "PNG"

  def icon2Binary(icon: Option[Icon]) = {
    icon.map(x => image2Binary(icon2Image(x))).getOrElse(Binary.Empty)
  }

  private def icon2Image(icon: Icon) = {
    val image = new BufferedImage(icon.getIconWidth, icon.getIconHeight, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()
    icon.paintIcon(null, graphics, 0, 0)
    graphics.dispose()
    image
  }

  private def image2Binary(image: BufferedImage): Binary = {
    val output = new ByteArrayOutputStream
    try {
      ImageIO.write(image, IMAGE_FORMAT, output)
      output.toByteArray
    } catch {
      case e: IOException =>
        e.printStackTrace()
        Binary.Empty
    }
  }
}

