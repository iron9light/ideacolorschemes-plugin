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
package impl

import org.bson.{BSONObject, BasicBSONObject}
import collection.JavaConversions._
import java.util.Date
import java.io.File
import org.fusesource.hawtdb.api.{BTreeIndexFactory, PageFileFactory}
import com.intellij.openapi.Disposable
import com.ideacolorschemes.commons.bson.{BsonParser, ColorSchemeIdParser}

/**
 * @author il
 */
class HawtSchemeBookManager extends SchemeBookManager with Disposable {
  private[this] val path = ideaConfigFolder + "book.db"

  private[this] val factory = new PageFileFactory
  factory.setFile(new File(path))
  factory.open()
  
  private[this] val db = {
    val page = factory.getPageFile
    val indexFactory = new BTreeIndexFactory[String, SchemeBook]
    indexFactory.setValueCodec(SchemeBookCodec)
    indexFactory.openOrCreate(page)
  } 
  
  def get(name: String) = synchronized {
    Option(db.get(name))
  }

  def getAll = synchronized {
    db.toList.map(_.getValue)
  }

  def remove(name: String) {
    synchronized {
      db.remove(name)
    }
  }

  def removeAll() {
    synchronized {
      db.clear()
    }
  }

  def put(schemeBook: SchemeBook) {
    synchronized {
      db.put(schemeBook.name, schemeBook)
    }
  }

  def contains(name: String) = synchronized {
    db.containsKey(name)
  }

  def dispose() {
    factory.close()
  }

  def getComponentName = "ideacolor.SchemeBookManager"
}

object SchemeBookCodec extends CodecBase[SchemeBook] with BsonParser[SchemeBook] {
  def toBson(book: SchemeBook) = {
    val SchemeBook(name, schemeIds, timestamp) = book
    val b = new BasicBSONObject()
    .append("name", name)
    .append("schemeIds", schemeIds.flatMap(ColorSchemeIdParser.toBson(_)).toArray)
    .append("timestamp", timestamp)
    
    Some(b)
  }
  
  def fromBson(bson: BSONObject) = {
    val name = bson.get("name").asInstanceOf[String]
    val schemeIds = bson.get("schemeIds").asInstanceOf[java.util.List[_]].toList.map(ColorSchemeIdParser.fromBson)
    val timestamp = bson.get("timestamp").asInstanceOf[Date]

    SchemeBook(name, schemeIds, timestamp)
  }
  
  def toBytes(o: SchemeBook) = {
    bsonEncoder.encode(toBson(o).get)
  }

  def fromBytes(bytes: Array[Byte]) = {
    fromBson(bsonDecoder.readObject(bytes))
  }
}
