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

