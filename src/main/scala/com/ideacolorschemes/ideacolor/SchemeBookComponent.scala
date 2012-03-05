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

import util.{IdeaUtil, Loggable}
import com.intellij.openapi.editor.colors.{EditorColorsScheme, EditorColorsListener}
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager}
import com.intellij.openapi.project.{ProjectManager, Project}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ide.AppLifecycleListener

/**
 * @author il
 */
class SchemeBookComponent extends ApplicationComponent with UserManager.Sub with Loggable with IdeaUtil {
  val schemeBookManager = service[SchemeBookManager]

  import schemeBookManager._

  def initComponent() {
    editorColorsManager.addEditorColorsListener(new EditorColorsListener {
      def globalSchemeChange(scheme: EditorColorsScheme) {
        val name = scheme.name
        currentBook = if (isBook(scheme) && contains(name)) {
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
