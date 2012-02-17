package com.ideacolorschemes.ideacolor

import org.apache.commons.httpclient.methods.{StringRequestEntity, PostMethod, GetMethod}
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpClient, HttpStatus}
import com.ideacolorschemes.commons.entities.{ColorScheme, ColorSchemeId}
import java.net.URLEncoder
import java.io.InputStreamReader
import net.liftweb.json._
import com.ideacolorschemes.commons.json.ColorSchemeFormats

/**
 * @author il
 */
object SiteServices {
  implicit val formats = ColorSchemeFormats

  def checkAuth(userId: String, key: String): Boolean = {
    val client = getHttpClient(userId, key)
    val uri = httpHost + "/api/auth/check"
    val method = new GetMethod(uri)
    try {
      client.executeMethod(method)
      method.getStatusCode match {
        case HttpStatus.SC_OK => true
        case _ => false
      }
    } catch {
      case e =>
        // todo: add log
        false
    } finally {
      method.releaseConnection()
    }
  }

  def addScheme(colorScheme: ColorScheme) = {
    val json = Serialization.write(colorScheme)
    val httpClient = new HttpClient()
    val httpPost = new PostMethod("http://localhost:8080/api/addscheme")
    httpPost.setRequestEntity(new StringRequestEntity(json, "text/json", "UTF-8"))
    try {
      httpClient.executeMethod(httpPost)
      if (httpPost.getStatusCode == HttpStatus.SC_SEE_OTHER) {
        val url = httpHost + httpPost.getResponseHeader("Location").getValue
        Some(url)
      } else {
        // todo: add log
        None
      }
    } catch {
      case e =>
        // todo: add log
        None
    } finally {
      httpPost.releaseConnection()
    }
  }

  def scheme(id: ColorSchemeId): Option[ColorScheme] = {
    def urlEncode(s: String) = URLEncoder.encode(s, "UTF-8")
    val url = "http://localhost:8080/api/scheme/%s/%s/%s/%s".format(urlEncode(id.author), urlEncode(id.name), urlEncode(id.version.toString()), urlEncode(id.target))
    val httpClient = new HttpClient
    val method = new GetMethod(url)
    try {
      httpClient.executeMethod(method)
      val reader = new InputStreamReader(method.getResponseBodyAsStream, "UTF-8")
      JsonParser.parse(reader).extractOpt[ColorScheme]
    } catch {
      case e =>
        // todo: add log
        None
    } finally {
      method.releaseConnection()
    }
  }
  
  def schemeBookNames(implicit userManager: UserManager = UserManager) = {
    val httpClient = getHttpClient
    val httpGet = new GetMethod("http://localhost:8080/api/auth/schemebooknames")
    val responseStr = try {
      httpClient.executeMethod(httpGet)
      Some(httpGet.getResponseBodyAsString)
      // todo: should not just return JValue
    } catch {
      case e =>
        // todo: add log
        None
    } finally {
      httpGet.releaseConnection()
    }
    
    responseStr.flatMap(parseOpt)
  }
  
  def schemeBook(bookName: String)(implicit userManager: UserManager = UserManager) = {
    val httpClient = getHttpClient
    val httpGet = new GetMethod(httpHost + "/api/auth/schemebook/" + urlEncode(bookName))
    val responseStr = try {
      httpClient.executeMethod(httpGet)
      Some(httpGet.getResponseBodyAsString)
    } catch {
      case e =>
        // todo: add log
        None
    } finally {
      httpGet.releaseConnection()
    }

    responseStr.flatMap {
      str => {
        parseOpt(str).flatMap(_.extractOpt[List[ColorSchemeId]])
      }
    }.getOrElse(Nil)
  }
  
  private def getHttpClient(implicit userManager: UserManager): HttpClient = {
    getHttpClient(userManager.userId, userManager.key)
  }

  private def getHttpClient(userId: String, key: String): HttpClient = {
    val client = new HttpClient
    client.getParams.setAuthenticationPreemptive(true)
    client.getState.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userId, key))
    client
  }
  
  private def urlEncode(s: String) = URLEncoder.encode(s, "UTF-8")
}
