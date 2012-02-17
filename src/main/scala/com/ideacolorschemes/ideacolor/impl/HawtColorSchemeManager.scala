package com.ideacolorschemes.ideacolor
package impl

import com.ideacolorschemes.commons.entities._
import java.io.File
import com.intellij.openapi.Disposable
import com.ideacolorschemes.commons.bson.ColorSchemeIdParser
import org.bson.{BasicBSONDecoder, BasicBSONEncoder}
import org.fusesource.hawtdb.api.{BTreeIndexFactory, PageFileFactory}
import com.google.common.primitives.UnsignedBytes

/**
 * @author il
 */

class HawtColorSchemeManager extends ColorSchemeManager with Disposable {
  private def bsonEncoder = new BasicBSONEncoder
  private def bsonDecoder = new BasicBSONDecoder

  private implicit def colorSchemeId2binary(colorSchemeId: ColorSchemeId) = {
    bsonEncoder.encode(ColorSchemeIdParser.toBson(colorSchemeId).get)
  }

  private implicit def binary2colorSchemeId(bytes: Array[Byte]) = {
    ColorSchemeIdParser.fromBson(bsonDecoder.readObject(bytes))
  }

  private implicit def colorScheme2binary(colorScheme: ColorScheme) = {
    bsonEncoder.encode(com.ideacolorschemes.commons.bson.ColorSchemeParser.toBson(colorScheme).get)
  }

  private implicit def binary2colorScheme(bytes: Array[Byte]) = {
    com.ideacolorschemes.commons.bson.ColorSchemeParser.fromBson(bsonDecoder.readObject(bytes))
  }

  val path = ideaConfigFolder + "scheme.db"

  val factory = new PageFileFactory
  factory.setFile(new File(path))
  factory.open()
  val page = factory.getPageFile
  val indexFactory = new BTreeIndexFactory[Array[Byte], Array[Byte]]
  indexFactory.setComparator(UnsignedBytes.lexicographicalComparator)
  val db = indexFactory.openOrCreate(page)

  def get(id: ColorSchemeId): Option[ColorScheme] = synchronized {
    Option(db.get(id)) match {
      case None =>
        cache(SiteServices.scheme(id))
      case Some(x) => Some(x)
    }
  }

  def cache(schemeOption: Option[ColorScheme]) = {
    schemeOption.foreach {
      case scheme => {
        db.put(scheme.id, scheme)
      }
    }

    schemeOption
  }

  def dispose() {
    println("dispose: " + getComponentName)
    factory.close()
  }

  def getComponentName = "ideacolor.ColorSchemeManager"
}