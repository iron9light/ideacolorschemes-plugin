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
import com.intellij.openapi.project.Project
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager}
import com.intellij.notification.{NotificationType, Notification, Notifications}
import util.IdeaUtil


/**
 * @author il
 */
class UpdateSchemeBooksAction extends AnAction with IdeaUtil {
  private[this] val schemeBookManager = service[SchemeBookManager]

  def actionPerformed(anActionEvent: AnActionEvent) {
    implicit val project = anActionEvent.getProject

    updateSchemeBooks
  }

  private[this] def updateSchemeBooks(implicit project: Project) {
    ProgressManager.getInstance().run(new Backgroundable(project, "Update color schemes", false) {
      def run(indicator: ProgressIndicator) {
        schemeBookManager.update() match {
          case false =>
            showNotice("Color schemes is up-to-date.")
          case true =>
            showNotice("Color schemes updated.")
        }
      }
    })
  }

  private[this] def showNotice(message: String)(implicit project: Project) {
    Notifications.Bus.notify(new Notification("color", "Update color schemes", message, NotificationType.INFORMATION), project)
  }
}
