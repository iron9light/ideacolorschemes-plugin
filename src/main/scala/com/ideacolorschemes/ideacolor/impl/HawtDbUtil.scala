package com.ideacolorschemes.ideacolor.impl

import org.fusesource.hawtdb.api.PageFileFactory
import java.io.File

/**
 * @author il
 */
trait HawtDbUtil {
  def newPageFileFactory(path: String) = {
    val factory = new PageFileFactory
    factory.setMappingSegementSize(1024*512)
    factory.setFile(new File(path))
    factory.open()
    factory
  }
}
