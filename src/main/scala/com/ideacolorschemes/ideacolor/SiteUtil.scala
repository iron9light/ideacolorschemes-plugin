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

import com.intellij.openapi.project.{ProjectManager, Project}
import actors.Actor
import com.intellij.openapi.progress.{Task, ProgressIndicator, ProgressManager}

/**
 * @author il
 */
object SiteUtil {
  def accessToSiteWithModalProgress[T](func: ProgressIndicator => T)(implicit project: Project = ProjectManager.getInstance.getDefaultProject): T = {
    val me = Actor.self
    ProgressManager.getInstance().run(new Task.Modal(project, "Access to ideacolorschemes", true) {
      def run(indicator: ProgressIndicator) {
        me ! func(indicator)
      }
    })
    
    me.receive{
      case result: T =>
        result
    }
  }
}