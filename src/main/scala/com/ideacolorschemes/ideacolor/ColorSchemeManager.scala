package com.ideacolorschemes.ideacolor

import com.ideacolorschemes.commons.entities._
import com.intellij.openapi.components.ServiceManager

/**
 * @author il
 * @version 11/9/11 7:31 PM
 */


trait ColorSchemeManager {
  def get(id: ColorSchemeId): Option[ColorScheme]
}

object ColorSchemeManager {
  def apply() = ServiceManager.getService(classOf[ColorSchemeManager])
}