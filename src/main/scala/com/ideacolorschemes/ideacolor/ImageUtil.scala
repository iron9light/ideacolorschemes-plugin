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

