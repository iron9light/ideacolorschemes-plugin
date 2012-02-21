package com.ideacolorschemes.ideacolor
package ui

import com.intellij.openapi.ui.Messages
import com.ideacolorschemes.ideacolor.SiteServices
import com.intellij.openapi.project.{Project, ProjectManager}
import com.ideacolorschemes.ideacolor.SiteUtil._

/**
 * @author il
 */
abstract class IdeaColorSettingsPanelUtil {
  def checkCredentialsAction() {
    if (checkCredentials(getUserId, getKey)) {
      Messages.showInfoMessage("Connection successful", "Success")
    }
    else {
      Messages.showErrorDialog("Cannot login to the ideacolorscheme using given credentials", "Failure")
    }
  }

  def getUserId: String

  def getKey: String

  def noticeTest = "<html>Do not have an account at ideacolorscheme.com? <a href=\"" + httpHost + "\">Sign up</a></html>"

  private[this] def checkCredentials(userId: String, key: String)(implicit project: Project = ProjectManager.getInstance.getDefaultProject): Boolean = {
    if (userId.isEmpty || key.isEmpty)
      false
    else {
      accessToSiteWithModalProgress {
        indicator => {
          indicator.setText("Trying to login to ideacolorschemes")
          SiteServices.checkAuth(userId, key)
        }
      }(project)
    }
  }
}
