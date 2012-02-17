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
  private val userManager = UserManager

  def getDisplayName = "IdeaColorSchemes"

  def getIcon: Icon = null

  def getHelpTopic = null

  def createComponent = {
    settingsPanel match {
      case Some(panel) =>
        reset()
        panel.getPanel
      case None =>
        val panel = new IdeaColorSettingsPanel
        settingsPanel = Some(panel)
        panel.getPanel
    }
  }

  def isModified = {
    settingsPanel match {
      case Some(panel) if panel.getUserId != userManager.userId || panel.getKey != userManager.key =>
        true
      case _ =>
        false
    }
  }

  def apply() {
    settingsPanel.foreach {
      panel => {
        userManager.userId = panel.getUserId
        userManager.key = panel.getKey
      }
    }
  }

  def reset() {
    settingsPanel.foreach{
      panel => {
        panel.setUserId(userManager.userId)
        panel.setKey(userManager.key)
      }
    }
  }

  def disposeUIResources() {
    settingsPanel = None
  }

  def getId = "settings.ideacolor"

  def enableSearch(option: String) = null
}