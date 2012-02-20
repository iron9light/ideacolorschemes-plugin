package com.ideacolorschemes.ideacolor

import util.Loggable
import com.intellij.openapi.editor.colors.{EditorColorsScheme, EditorColorsListener}
import com.intellij.openapi.components.{ServiceManager, ApplicationComponent}
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager}
import com.intellij.openapi.project.Project
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ide.AppLifecycleListener

/**
 * @author il
 */
class SchemeBookComponent extends ApplicationComponent with Loggable {
  val schemeBookManager = ServiceManager.getService(classOf[SchemeBookManager])

  import schemeBookManager._

  def initComponent() {
    editorColorsManager.addEditorColorsListener(new EditorColorsListener {
      def globalSchemeChange(scheme: EditorColorsScheme) {
        val name = scheme.getName
        if (contains(name)) {
          currentBook = Some(name)
        }
      }
    })

    UserManager.subscribe(schemeBookManager)

    val listener: AppLifecycleListener = new AppLifecycleListener.Adapter {
      override def appStarting(projectFromCommandLine: Project) {
        ProgressManager.getInstance().run(new Backgroundable(projectFromCommandLine, "Update color schemes", false) {
          def run(indicator: ProgressIndicator) {
            initUpdate()
          }
        })
      }
    }

    ApplicationManager.getApplication.getMessageBus.connect().subscribe(AppLifecycleListener.TOPIC, listener)
  }

  def disposeComponent() {
  }

  def getComponentName = "ideacolor.SchemeBookManager"
}
