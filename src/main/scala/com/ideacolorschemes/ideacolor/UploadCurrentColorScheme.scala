package com.ideacolorschemes.ideacolor

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import net.liftweb.json.Serialization
import com.ideacolorschemes.commons.json.ColorSchemeFormats
import com.intellij.openapi.editor.colors.{EditorColorsScheme, EditorColorsManager}
import com.intellij.ide.BrowserUtil
import org.apache.commons.httpclient.methods.{StringRequestEntity, PostMethod}
import org.apache.commons.httpclient.{HttpStatus, HttpClient}
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.progress.ProgressManager


/**
 * @author il
 */

class UploadCurrentColorScheme extends AnAction {
  def ideaEditorColorsManager = EditorColorsManager.getInstance
  
  def actionPerformed(anActionEvent: AnActionEvent) {
    val editorColorsScheme = ideaEditorColorsManager.getGlobalScheme
    if (ideaEditorColorsManager.isDefaultScheme(editorColorsScheme)) {
      Messages.showInfoMessage("Current color scheme is default.", "Failure")
    } else {
      updateScheme(editorColorsScheme)
    }
  }
  
  def updateScheme(editorColorsScheme: EditorColorsScheme) {
    val colorScheme = ColorSchemeParser.parse(editorColorsScheme).get
    val json = Serialization.write(colorScheme)(ColorSchemeFormats)
    SiteUtil.accessToSiteWithModalProgress {
      ProgressManager.getInstance().getProgressIndicator.setText("Trying to update current color scheme to ideacolorschemes")
      updateScheme(json)
    } match {
      case Some(url) =>
        BrowserUtil.launchBrowser(url)
      case None =>
        Messages.showInfoMessage("Cannot update current color scheme to ideacolorschemes.", "Failure")
    }
  }
  
  def updateScheme(json: String) = {
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
}

