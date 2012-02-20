package com.ideacolorschemes.ideacolor

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import com.intellij.openapi.editor.colors.{EditorColorsScheme, EditorColorsManager}
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.project.Project


/**
 * @author il
 */

class UploadCurrentColorSchemeAction extends AnAction {
  def ideaEditorColorsManager = EditorColorsManager.getInstance
  
  def actionPerformed(anActionEvent: AnActionEvent) {
    implicit val project = anActionEvent.getProject
    val editorColorsScheme = ideaEditorColorsManager.getGlobalScheme
    if (ideaEditorColorsManager.isDefaultScheme(editorColorsScheme)) {
      Messages.showInfoMessage(project, "Current color scheme is default.", "Failure")
    } else {
      uploadScheme(editorColorsScheme)
    }
  }
  
  def uploadScheme(editorColorsScheme: EditorColorsScheme)(implicit project: Project) {
    val colorScheme = ColorSchemeParser.parse(editorColorsScheme).get
    SiteUtil.accessToSiteWithModalProgress {
      indicator => {
        indicator.setText("Trying to update current color scheme to ideacolorschemes")
        SiteServices.addScheme(colorScheme)
      }
    } match {
      case Some(url) =>
        BrowserUtil.launchBrowser(url)
      case None =>
        Messages.showInfoMessage(project, "Cannot update current color scheme to ideacolorschemes.", "Failure")
    }
  }
}

