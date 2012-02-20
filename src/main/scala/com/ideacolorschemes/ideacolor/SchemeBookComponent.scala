package com.ideacolorschemes.ideacolor

import util.Loggable
import com.intellij.openapi.editor.colors.{EditorColorsScheme, EditorColorsListener}
import com.intellij.openapi.components.{ServiceManager, ApplicationComponent}
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager}
import com.intellij.openapi.project.{ProjectManager, Project}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ide.AppLifecycleListener

/**
 * @author il
 */
class SchemeBookComponent extends ApplicationComponent with UserManager.Sub with Loggable {
  val schemeBookManager = ServiceManager.getService(classOf[SchemeBookManager])

  import schemeBookManager._

  def initComponent() {
    editorColorsManager.addEditorColorsListener(new EditorColorsListener {
      def globalSchemeChange(scheme: EditorColorsScheme) {
        val name = scheme.getName
        currentBook = if (contains(name)) {
          Some(name)
        } else {
          None
        }
      }
    })

    UserManager.subscribe(this)

    val listener: AppLifecycleListener = new AppLifecycleListener.Adapter {
      override def appStarting(projectFromCommandLine: Project) {
        runInitUpdate(projectFromCommandLine)
      }
    }

    ApplicationManager.getApplication.getMessageBus.connect().subscribe(AppLifecycleListener.TOPIC, listener)
  }

  def disposeComponent() {
  }

  def getComponentName = "ideacolor.SchemeBookManager"

  def notify(pub: UserManager.Pub, event: String) {
    reset()

    runInitUpdate()
  }

  private def runInitUpdate(implicit project: Project = ProjectManager.getInstance.getDefaultProject) {
    ProgressManager.getInstance().run(new Backgroundable(project, "Update color schemes", false) {
      def run(indicator: ProgressIndicator) {
        initUpdate()
      }
    })
  }
}
