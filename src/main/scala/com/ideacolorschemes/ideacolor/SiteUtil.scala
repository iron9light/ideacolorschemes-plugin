package com.ideacolorschemes.ideacolor

import com.intellij.openapi.project.Project
import actors.Actor
import com.intellij.openapi.progress.{Task, ProgressIndicator, ProgressManager}
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.{HttpStatus, UsernamePasswordCredentials, HttpClient}

/**
 * @author il
 */
object SiteUtil {
  def getHttpClient(userId: String, key: String) = {
    val client = new HttpClient
    client.getParams.setAuthenticationPreemptive(true)
    client.getState.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userId, key))
    client
  }
  
  def testConnection(userId: String, key: String): Boolean = {
    val client = getHttpClient(userId, key)
    val uri = "http://localhost:8080/api/auth/check"
    val method = new GetMethod(uri)
    try {
      client.executeMethod(method)
      method.getStatusCode match {
        case HttpStatus.SC_OK => true
        case _ => false
      }
    } catch {
      case e => false
    } finally {
      method.releaseConnection()
    }
  }
  
  def checkCredentials(userId: String, key: String, project: Project): Boolean = {
    if (userId.isEmpty || key.isEmpty)
      false
    else {
      accessToSiteWithModalProgress{
        ProgressManager.getInstance().getProgressIndicator.setText("Trying to login to ideacolorschemes")
        testConnection(userId, key)
      }(project)
    }
  }
  
  def accessToSiteWithModalProgress[T](func: => T)(project: Project): T = {
    val me = Actor.self
    ProgressManager.getInstance().run(new Task.Modal(project, "Access to ideacolorschemes", true) {
      def run(indicator: ProgressIndicator) {
        me ! func 
      }
    })
    
    me.receive{
      case result: T =>
        result
    }
  }
}