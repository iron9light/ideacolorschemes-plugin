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