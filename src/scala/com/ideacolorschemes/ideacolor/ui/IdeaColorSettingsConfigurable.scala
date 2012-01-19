package com.ideacolorschemes.ideacolor
package ui

import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.Icon

/**
 * @author il
 * @version 12/17/11 11:15 PM
 */

class IdeaColorSettingsConfigurable extends SearchableConfigurable {
  private var settingsPanel: Option[IdeaColorSettingsPanel] = None
  private val settings = UserManager.userSettings

  def getDisplayName = "IdeaColorSchemes"

  def getIcon: Icon = null

  def getHelpTopic = null

  def createComponent = {
    if (settingsPanel.isEmpty) {
      settingsPanel = Some(new IdeaColorSettingsPanel)
    }
    reset()
    settingsPanel.get.getPanel
  }

  def isModified = {
    settingsPanel match {
      case Some(panel) if panel.getUserId != settings.userId || panel.getKey != settings.key =>
        true
      case _ =>
        false
    }
  }

  def apply() {
    settingsPanel.foreach {
      panel => {
        settings.userId = panel.getUserId
        settings.key = panel.getKey
      }
    }
  }

  def reset() {
    settingsPanel.foreach{
      panel => {
        panel.setUserId(settings.userId)
        panel.setKey(settings.key)
      }
    }
  }

  def disposeUIResources() {
    settingsPanel = None
  }

  def getId = "settings.ideacolor"

  def enableSearch(option: String) = null
}