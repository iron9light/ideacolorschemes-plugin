package com.ideacolorschemes.ideacolor

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import net.liftweb.json.Serialization
import com.ideacolorschemes.commons.json.ColorSchemeFormats
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.ide.BrowserUtil
import org.apache.commons.httpclient.methods.{StringRequestEntity, PostMethod}
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.{UsernamePasswordCredentials, HttpStatus, HttpClient}


/**
 * @author il
 * @version 11/7/11 9:53 PM
 */

class UploadCurrentColorScheme extends AnAction {
  def actionPerformed(anActionEvent: AnActionEvent) {
    val editorColorsScheme = EditorColorsManager.getInstance.getGlobalScheme
    // TODO: check if it's default.
    val colorScheme = ColorSchemeParser.parse(editorColorsScheme).get
    val json = Serialization.write(colorScheme)(ColorSchemeFormats)
    val httpClient = new HttpClient()
    val httpPost = new PostMethod("http://localhost:8080/api/addscheme")
    httpPost.setRequestEntity(new StringRequestEntity(json, "text/json", "UTF-8"))
    try {
      httpClient.executeMethod(httpPost)
      if (httpPost.getStatusCode == HttpStatus.SC_SEE_OTHER) {
        val url = "http://localhost:8080" + httpPost.getResponseHeader("Location").getValue
        BrowserUtil.launchBrowser(url)
      }
    } finally {
      httpPost.releaseConnection()
    }
  }
}

