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

package com.ideacolorschemes

import com.intellij.openapi.application.PathManager
import java.io.File

/**
 * @author il
 */
package object ideacolor {
  val host = "localhost:8080"

  val httpHost = "http://" + host

  final val configFolder = "ideacolorschemes"

  val ideaConfigFolder = PathManager.getOptionsPath + File.separatorChar + configFolder + File.separatorChar
}
