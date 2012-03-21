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

import com.intellij.openapi.actionSystem.{AnActionEvent, AnAction}
import com.intellij.openapi.editor.colors.{EditorColorsScheme, EditorColorsManager}
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.project.Project
import com.ideacolorschemes.commons.entities.ColorScheme


/**
 * @author il
 */

class UploadCurrentColorSchemeAction extends AnAction with IdeaSchemeNameUtil {
  def ideaEditorColorsManager = EditorColorsManager.getInstance

  def actionPerformed(anActionEvent: AnActionEvent) {
    implicit val project = anActionEvent.getProject
    val editorColorsScheme = ideaEditorColorsManager.getGlobalScheme
    if (ideaEditorColorsManager.isDefaultScheme(editorColorsScheme)) {
      Messages.showInfoMessage(project, "Current color scheme is default.", "Failure")
    } else if (isBook(editorColorsScheme)) {
      editorColorsScheme match {
        case colorScheme: EditableIdeaColorScheme if colorScheme.change.isDefined =>
          uploadScheme(colorScheme.change.get)
        case _ =>
          Messages.showInfoMessage(project, "Current color scheme is from website.", "Failure")
      }
    } else {
      uploadScheme(editorColorsScheme)
    }
  }

  def uploadScheme(editorColorsScheme: EditorColorsScheme)(implicit project: Project) {
    val colorScheme = ColorSchemeParser.parse(editorColorsScheme).get
    uploadScheme(colorScheme)
  }

  def uploadScheme(colorScheme: ColorScheme)(implicit project: Project) {
    SiteUtil.accessToSiteWithModalProgress {
      indicator => {
        indicator.setText("Trying to upload current color scheme to ideacolorschemes")
        SiteServices.addScheme(colorScheme)
      }
    } match {
      case Some(url) =>
        BrowserUtil.launchBrowser(url)
      case None =>
        Messages.showInfoMessage(project, "Cannot upload current color scheme to ideacolorschemes.", "Failure")
    }
  }
}

