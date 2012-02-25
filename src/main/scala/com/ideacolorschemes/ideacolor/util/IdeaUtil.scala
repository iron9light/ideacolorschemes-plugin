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

package com.ideacolorschemes.ideacolor.util

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.application.{ApplicationManager, Application}

/**
 * @author il
 */
trait IdeaUtil {
  def service[T](implicit mf: Manifest[T], project: Project = null) = {
    val clazz = mf.erasure.asInstanceOf[Class[T]]
    if (project == null) {
      ServiceManager.getService(clazz)
    } else {
      ServiceManager.getService(project, clazz)
    }
  }
  
  def ideaRun(f: => Unit)(implicit application: Application = ApplicationManager.getApplication) {
    if (application.isDispatchThread) {
      f
    } else {
      application.invokeLater(new Runnable {
        def run() {
          f
        }
      })
    }
  }
}
