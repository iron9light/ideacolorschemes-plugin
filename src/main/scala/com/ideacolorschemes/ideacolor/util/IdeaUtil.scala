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
