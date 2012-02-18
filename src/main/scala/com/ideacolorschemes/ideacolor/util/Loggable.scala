package com.ideacolorschemes.ideacolor.util

import com.intellij.openapi.diagnostic.Logger

/**
 * @author il
 */
trait Loggable {
  val logger = Logger.getInstance(this.getClass)
}
