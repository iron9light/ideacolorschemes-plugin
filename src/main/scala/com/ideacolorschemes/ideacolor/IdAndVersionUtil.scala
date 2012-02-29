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

import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.ide.plugins.PluginManager

object IdAndVersionUtil {
  lazy val applicationIdAndVersion = {
    val applicationInfo = ApplicationInfo.getInstance
    val id = applicationInfo.getVersionName
    val version = applicationInfo.getMajorVersion + "." + applicationInfo.getMinorVersion
    (id, Some(version))
  }

  def idAndVersion(page: ColorSettingsPage) = {
    Option(PluginManager.getPluginByClassName(page.getClass.getName)) match {
      case Some(pluginId) =>
        val id = pluginId.getIdString
        val plugin = PluginManager.getPlugin(pluginId)
        val version = Option(plugin.getVersion)
        (id, version)
      case None => applicationIdAndVersion
    }
  }
}

