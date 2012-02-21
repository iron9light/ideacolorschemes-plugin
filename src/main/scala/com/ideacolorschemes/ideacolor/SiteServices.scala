package com.ideacolorschemes.ideacolor

import com.ideacolorschemes.commons.entities.{ColorScheme, ColorSchemeId}
import net.liftweb.json._
import com.ideacolorschemes.commons.json.ColorSchemeFormats
import util.Loggable
import java.util.Date
import dispatch._

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
      val client = super.make_client.asInstanceOf[ConfiguredHttpClient]
      client.setRedirectStrategy(new DefaultRedirectStrategy{
        override def isRedirected(req: HttpRequest, res: HttpResponse, ctx: HttpContext) = false
      })
      client
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
