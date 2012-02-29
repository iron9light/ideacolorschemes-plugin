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

import com.ideacolorschemes.commons.entities.{ColorScheme, ColorSchemeId}
import net.liftweb.json._
import com.ideacolorschemes.commons.json.ColorSchemeFormats
import util.Loggable
import java.util.Date
import dispatch._
import org.apache.http.params.HttpParams
import org.apache.http.conn.params.ConnRouteParams
import org.apache.http.HttpHost
import org.apache.http.auth.{NTCredentials, UsernamePasswordCredentials, AuthScope}

/**
 * @author il
 */
object SiteServices extends Loggable {
  implicit val formats = ColorSchemeFormats
  
  private val h = {
    val ss = host.split(":")
    if (ss.length > 1) {
      :/(ss(0), ss(1).toInt)
    } else {
      :/(host)
    }
  }
  
  private val noRedirectHttp = new Http {
    import org.apache.http.impl.client.DefaultRedirectStrategy
    import org.apache.http.{HttpResponse, HttpRequest}
    import org.apache.http.protocol.HttpContext

    override def make_client = {
      new ConfiguredHttpClient(credentials) {
        override protected def configureProxy(params: HttpParams) = {
          val sys = System.getProperties
          val host = sys.getProperty("https.proxyHost",
            sys.getProperty("http.proxyHost"))
          val port = sys.getProperty("https.proxyPort",
            sys.getProperty("http.proxyPort"))
          val user = sys.getProperty("https.proxyUser",
            sys.getProperty("http.proxyUser"))
          val password = sys.getProperty("https.proxyPassword",
            sys.getProperty("http.proxyPassword"))
          val domain = sys.getProperty("https.auth.ntlm.domain",
            sys.getProperty("http.auth.ntlm.domain"))
          if (host != null && !host.isEmpty && port != null && !port.isEmpty) {
            ConnRouteParams.setDefaultProxy(params,
              new HttpHost(host, port.toInt))
            proxyScope = Some(new AuthScope(host, port.toInt))
          }
          if (user != null && !user.isEmpty && password != null && !password.isEmpty) {
            proxyBasicCredentials =
              Some(new UsernamePasswordCredentials(user, password))
            // We should pass our hostname, actually
            // Also, we ought to support "domain/user" syntax
            proxyNTCredentials = Some(new NTCredentials(
              user, password, "", Option(domain) getOrElse ""))
          }
          params
        }

        setRedirectStrategy(new DefaultRedirectStrategy{
          override def isRedirected(req: HttpRequest, res: HttpResponse, ctx: HttpContext) = false
        })
      }
    }
  }

  def checkAuth(userId: String, key: String): Boolean = {
    try {
      Http((h / "api" / "auth" / "check" as_! (userId, key)).>|.~>{_ => true})
    } catch {
      case _ => false
    }
  }

  def addScheme(colorScheme: ColorScheme): Option[String] = {
    val json = Serialization.write(colorScheme)

    try {
      noRedirectHttp.when(_ == org.apache.http.HttpStatus.SC_SEE_OTHER)((h / "api" / "addscheme") << (json, "text/json") >\ "UTF-8" >:> {
        head => head.get("Location").flatMap(_.headOption).map(httpHost + _)
      })
    } catch {
      case _ => None
    }
  }

  def scheme(id: ColorSchemeId): Option[ColorScheme] = {
    try {
      Http((h / "api" / "scheme" / id.author / id.name / id.version.toString / id.target).gzip >>~ {
        reader => {
          JsonParser.parseOpt(reader).flatMap(_.extractOpt[ColorScheme])
        }
      })
    } catch {
      case _ => None
    }
  }

  private case class NameAndTimestamp(name: String, timestamp: Long)
  def schemeBookNames(implicit userManager: UserManager = UserManager): Option[List[(String, Date)]] = {
    try {
      Http((h / "api" / "auth" / "schemebooknames").as_!(userManager.userId, userManager.key).gzip >>~ {
        reader => {
          JsonParser.parseOpt(reader).flatMap {
            case JArray(list: List[JObject]) =>
              Some(list.map(_.extract[NameAndTimestamp]).map {
                case NameAndTimestamp(name, timestamp) =>
                  (name, new Date(timestamp))
              })
            case _ =>
              None
          }
        }
      })
    } catch {
      case _ => None
    }
  }
  
  def schemeBook(bookName: String)(implicit userManager: UserManager = UserManager): Option[List[ColorSchemeId]] = {
    try {
      Http((h / "api" / "auth" / "schemebook" / bookName).as_!(userManager.userId, userManager.key).gzip >>~ {
        reader => {
          JsonParser.parseOpt(reader).flatMap(_.extractOpt[List[ColorSchemeId]])
        }
      })
    } catch {
      case _ => None
    }
  }
}
