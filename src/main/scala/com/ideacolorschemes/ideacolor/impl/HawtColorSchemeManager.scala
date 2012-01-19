package com.ideacolorschemes.ideacolor
package impl

import java.lang.String
import com.ideacolorschemes.commons.entities._
import com.intellij.openapi.application.PathManager
import java.net.URLEncoder
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import java.io.{InputStreamReader, File}
import net.liftweb.json.Serialization
import com.intellij.openapi.Disposable
import com.ideacolorschemes.commons.bson.ColorSchemeIdParser
import org.bson.{BasicBSONDecoder, BasicBSONEncoder}
import org.fusesource.hawtdb.api.{BTreeIndexFactory, PageFileFactory}
import com.google.common.primitives.UnsignedBytes

/**
 * @author il
 * @version 11/9/11 7:31 PM
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


  val path = PathManager.getOptionsPath + File.separatorChar + "ideacolor.db"

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
        cache(remoteGet(id))
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

  def remoteGet(id: ColorSchemeId): Option[ColorScheme] = {
    implicit val formats = com.ideacolorschemes.commons.json.ColorSchemeFormats
    def urlEncode(s: String) = URLEncoder.encode(s, "UTF-8")
    val url = "http://localhost:8080/api/scheme/%s/%s/%s/%s".format(urlEncode(id.author), urlEncode(id.name), urlEncode(id.version.toString()), urlEncode(id.target))
    val httpClient = new HttpClient
    val method = new GetMethod(url)
    try {
      httpClient.executeMethod(method)
      val reader = new InputStreamReader(method.getResponseBodyAsStream, "UTF-8")
      Some(Serialization.read[ColorScheme](reader))
    } catch {
      case ignore =>
        None
    } finally {
      method.releaseConnection()
    }
  }

  def dispose() {
    println("dispose: " + getComponentName)
    factory.close()
  }

  def getComponentName = "ideacolor.ColorSchemeManager"
}