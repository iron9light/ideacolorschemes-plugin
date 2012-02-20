package com.ideacolorschemes

import com.intellij.openapi.application.PathManager
import java.io.File

/**
 * @author il
 */
package object ideacolor {
  val host = "ideacolorschemes.com"

  val httpHost = "http://" + host

  final val configFolder = "ideacolorschemes"

  val ideaConfigFolder = PathManager.getOptionsPath + File.separatorChar + configFolder + File.separatorChar
}
