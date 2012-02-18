package com.ideacolorschemes.ideacolor

import util.Loggable
import com.intellij.openapi.components.ApplicationComponent

/**
* @author il
*/
class SchemeBookComponent extends SchemeBookManager with ApplicationComponent with Loggable {
  def initComponent() {
    addBooks()
  }

  def disposeComponent() {
  }

  def getComponentName = "ideacolor.SchemeBookManager"
}
