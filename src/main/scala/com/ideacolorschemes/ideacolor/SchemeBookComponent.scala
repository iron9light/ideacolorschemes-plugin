package com.ideacolorschemes.ideacolor

import util.Loggable
import com.intellij.openapi.editor.colors.{EditorColorsScheme, EditorColorsListener}
import com.intellij.openapi.components.{ServiceManager, ApplicationComponent}

/**
* @author il
*/
class SchemeBookComponent extends ApplicationComponent with Loggable {
  val schemeBookManager = ServiceManager.getService(classOf[SchemeBookManager])
  import schemeBookManager._

  def initComponent() {
    editorColorsManager.addEditorColorsListener(new EditorColorsListener{
      def globalSchemeChange(scheme: EditorColorsScheme) {
        val name = scheme.getName
        if (contains(name)) {
          currentBook = Some(name)
        }
      }
    })

    UserManager.subscribe(schemeBookManager)

    // todo: run async
    initUpdate()
  }

  def disposeComponent() {
  }

  def getComponentName = "ideacolor.SchemeBookManager"
}
