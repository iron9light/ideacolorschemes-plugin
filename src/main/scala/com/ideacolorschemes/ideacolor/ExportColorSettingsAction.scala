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
import com.intellij.openapi.options.colors.ColorSettingsPages
import javax.swing.JFileChooser
import net.liftweb.json.Serialization
import com.intellij.openapi.ui.Messages
import java.io.{FileWriter, File}
import com.ideacolorschemes.commons.json.{ColorSchemeFormats, ColorSettingsPageObjectFormats}


/**
 * @author il
 * @version 11/7/11 9:53 PM
 */

class ExportColorSettingsAction extends AnAction {
  private val COLOR_SETTINGS_FILE_NAME = "ColorSettings.json"
  private val COLOR_SCHEMES_FILE_NAME = "DefaultColorSchemes.json"

  def actionPerformed(anActionEvent: AnActionEvent) {
    val chooser = new JFileChooser
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
    chooser.setAcceptAllFileFilterUsed(false)
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      val dir = chooser.getSelectedFile
      val colorSettingsFile = new File(dir, COLOR_SETTINGS_FILE_NAME)
      val colorSchemesFile = new File(dir, COLOR_SCHEMES_FILE_NAME)
      lazy val colorSettingsWriter = new FileWriter(colorSettingsFile)
      lazy val colorSchemesWriter = new FileWriter(colorSchemesFile)
      try {
        val pages = ColorSettingsPages.getInstance.getRegisteredPages.toList
        val colorSettings = pages.map(ColorSettingsPageExtractor.extract)
        val colorSchemes = pages.map(DefaultColorSchemeExtractor.extract)

        Serialization.write(colorSettings, colorSettingsWriter)(ColorSettingsPageObjectFormats)
        Serialization.write(colorSchemes, colorSchemesWriter)(ColorSchemeFormats)
        Messages.showInfoMessage("Your color settings have been successfully exported.", "Export Complete")
      } catch {
        case e: Exception =>
          Messages.showErrorDialog("Error writing color settings.\n\n" + e.getLocalizedMessage, "Error Writing File")
          e.printStackTrace()
      }
    }
  }
}