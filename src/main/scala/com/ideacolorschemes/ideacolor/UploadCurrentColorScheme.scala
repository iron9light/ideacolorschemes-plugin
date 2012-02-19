package com.ideacolorschemes.ideacolor

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import com.intellij.openapi.editor.colors.{EditorColorsScheme, EditorColorsManager}
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.ui.Messages


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
    SiteUtil.accessToSiteWithModalProgress {
      indicator => {
        indicator.setText("Trying to update current color scheme to ideacolorschemes")
        SiteServices.addScheme(colorScheme)
      }
    } match {
      case Some(url) =>
        BrowserUtil.launchBrowser(url)
      case None =>
        Messages.showInfoMessage("Cannot update current color scheme to ideacolorschemes.", "Failure")
    }
  }
}

