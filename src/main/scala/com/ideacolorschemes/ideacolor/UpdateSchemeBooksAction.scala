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
