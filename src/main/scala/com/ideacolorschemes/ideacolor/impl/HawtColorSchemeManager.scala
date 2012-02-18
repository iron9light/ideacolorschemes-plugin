package com.ideacolorschemes.ideacolor
package impl

import com.ideacolorschemes.commons.entities._
import com.intellij.openapi.Disposable
import com.ideacolorschemes.commons.bson.ColorSchemeIdParser
import org.bson.{BasicBSONDecoder, BasicBSONEncoder}
import java.io.{DataOutput, DataInput, File}
import org.fusesource.hawtbuf.codec.{BytesCodec, Codec}
import org.fusesource.hawtdb.api.{BTreeIndexFactory, PageFileFactory}
import java.util.Comparator

/**
 * @author il
 */

abstract class CodecBase[T] extends Codec[T] {
  protected def bsonEncoder = new BasicBSONEncoder
  protected def bsonDecoder = new BasicBSONDecoder

  private[this] def bytesCodec = BytesCodec.INSTANCE

  def encode(o: T, dataOut: DataOutput) {
    val bytes = toBytes(o)
    bytesCodec.encode(bytes, dataOut)
  }

  def decode(dataIn: DataInput) = {
    val bytes = bytesCodec.decode(dataIn)
    fromBytes(bytes)
  }

  def getFixedSize = -1

  def isEstimatedSizeSupported = false

  def estimatedSize(o: T) = throw new UnsupportedOperationException

  def isDeepCopySupported = true

  def deepCopy(source: T) = source

  def toBytes(o: T): Array[Byte]

  def fromBytes(bytes: Array[Byte]): T
}

object ColorSchemeIdCodec extends CodecBase[ColorSchemeId] {
  def toBytes(o: ColorSchemeId): Array[Byte] = {
    bsonEncoder.encode(ColorSchemeIdParser.toBson(o).get)
  }
  
  def fromBytes(bytes: Array[Byte]): ColorSchemeId = {
    ColorSchemeIdParser.fromBson(bsonDecoder.readObject(bytes))
  }
}

object ColorSchemeCodec extends CodecBase[ColorScheme] {
  def toBytes(o: ColorScheme): Array[Byte] = {
    bsonEncoder.encode(com.ideacolorschemes.commons.bson.ColorSchemeParser.toBson(o).get)
  }

  def fromBytes(bytes: Array[Byte]): ColorScheme = {
    com.ideacolorschemes.commons.bson.ColorSchemeParser.fromBson(bsonDecoder.readObject(bytes))
  }
}

object ColorSchemeIdComparator extends Comparator[ColorSchemeId] {
  def compare(o1: ColorSchemeId, o2: ColorSchemeId) = {
    if (o1 eq o2) {
      0
    } else if (o1.author != o2.author) {
      o1.author.compareTo(o2.author)
    } else if (o1.name != o2.name) {
      o1.name.compareTo(o2.name)
    } else if (o1.target != o2.target) {
      o1.target.compareTo(o2.target)
    } else {
      VersionComparator.compare(o1.version, o2.version)
    }
  }
}

object VersionComparator extends Comparator[Version] {
  def compare(o1: Version, o2: Version) = {
    if (o1.major != o2.major) {
      o1.major - o2.major
    } else if (o1.minor != o2.minor) {
      o1.minor - o2.minor
    } else if (o1.isRelease != o2.isRelease) {
      if (o1.isRelease) 1 else -1
    } else {
      o1.incremental.getOrElse(-1) - o2.incremental.getOrElse(-1)
    }
  }
}

class HawtColorSchemeManager extends ColorSchemeManager with Disposable {
  val path = ideaConfigFolder + "scheme.db"

  val factory = new PageFileFactory
  factory.setFile(new File(path))
  factory.open()

  val db = {
    val page = factory.getPageFile
    val indexFactory = new BTreeIndexFactory[ColorSchemeId, ColorScheme]
    indexFactory.setKeyCodec(ColorSchemeIdCodec)
    indexFactory.setValueCodec(ColorSchemeCodec)
    indexFactory.setComparator(ColorSchemeIdComparator)
    indexFactory.openOrCreate(page)
  }

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
    factory.close()
  }

  def getComponentName = "ideacolor.ColorSchemeManager"
}