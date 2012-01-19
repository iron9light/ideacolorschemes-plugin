package com.ideacolorschemes.ideacolor

import com.intellij.openapi.startup.StartupActivity
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpClient}
import com.intellij.openapi.project.{DumbAware, Project}
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ide.util.TipDialog
import com.intellij.openapi.ui.Messages
import java.nio.charset.Charset
import java.io.StringWriter
import com.intellij.openapi.components.ServiceDescriptor
import net.liftweb.json._
import org.apache.commons.httpclient.methods.GetMethod
import java.net.URLEncoder
import com.ideacolorschemes.commons.entities.ColorSchemeId
import com.ideacolorschemes.commons.json.ColorSchemeFormats
import com.intellij.openapi.editor.colors.EditorColorsManager

/**
 * @author il
 * @version 12/20/11 9:39 AM
 */

class SchemeBookManager {
  implicit val formats = ColorSchemeFormats
  
  def httpClient = {
    val client = new HttpClient
    client.getState.setCredentials(
      new AuthScope("localhost", 8080, "api"),
      new UsernamePasswordCredentials(UserManager.userId, UserManager.key)
    )
    client
  }
  
  def books = {
    val httpGet = new GetMethod("http://localhost:8080/api/auth/schemebooknames")
    httpGet.setDoAuthentication(true)
    val s = try {
      httpClient.executeMethod(httpGet)
      httpGet.getResponseBodyAsString
    } finally {
      httpGet.releaseConnection()
    }

    parseOpt(s) match {
      case Some(JArray(list: List[JObject])) =>
        for {
          book <- list
        } yield {
          val bookName = book.values("name").toString
          val httpGet2 = new GetMethod("http://localhost:8080/api/auth/schemebook/" + urlEncode(bookName))
          httpGet2.setDoAuthentication(true)
          val s2 = try {
            httpClient.executeMethod(httpGet2)
            httpGet2.getResponseBodyAsString
          } finally {
            httpGet2.releaseConnection()
          }
          (bookName, Serialization.read[List[ColorSchemeId]](s2))
        }
      case _ =>
        Nil
    }
  }
  
  def addBooks() {
    val editorColorsManager = EditorColorsManager.getInstance()
    for {
      (bookName, ids) <- books
    } {
      editorColorsManager.addColorsScheme(new IdeaColorScheme(bookName, ids))
    }
  }

  def urlEncode(s: String) = URLEncoder.encode(s, "UTF-8")
}

object SchemeBookManager extends SchemeBookManager

//class LoadColorSchemeBooks extends StartupActivity with DumbAware {
//  private var myVeryFirstProjectOpening = true
//  
//  def runActivity(project: Project) {
//    if(!myVeryFirstProjectOpening) {
//      return
//    }
//    myVeryFirstProjectOpening = false
//
//
//
//
//    ToolWindowManager.getInstance(project).invokeLater(new Runnable {
//      def run() {
//        if (project.isDisposed) return
//        ToolWindowManager.getInstance(project).invokeLater(new Runnable {
//          def run() {
//            if (project.isDisposed) return
//            Messages.showInfoMessage(this.getClass.toString, "load")
//          }
//        })
//      }
//    })
//
//    println(this.getClass)
//    val httpClient = new HttpClient
//    httpClient.getState.setCredentials(
//      new AuthScope("localhost", 8080, "api"),
//      new UsernamePasswordCredentials(UserManager.userId, UserManager.key)
//    )
//    val httpGet = new GetMethod("http://localhost:8080/api/auth/schemebooknames")
//    httpGet.setDoAuthentication(true)
//    try {
//      httpClient.executeMethod(httpGet)
//      println(httpGet.getResponseBodyAsString)
//    } finally {
//      httpGet.releaseConnection()
//    }
//  }
//}

object BookManager {
  def xxx() {
    println(this.getClass)
    val httpClient = new HttpClient
    httpClient.getState.setCredentials(
      new AuthScope("localhost", 8080, "api"),
      new UsernamePasswordCredentials(UserManager.userId, UserManager.key)
    )
    val httpGet = new GetMethod("http://localhost:8080/api/auth/schemebooknames")
    httpGet.setDoAuthentication(true)
    try {
      httpClient.executeMethod(httpGet)
      println(httpGet.getResponseBodyAsString(Int.MaxValue))
    } finally {
      httpGet.releaseConnection()
    }
  }
}