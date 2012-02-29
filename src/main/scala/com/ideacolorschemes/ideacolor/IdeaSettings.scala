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

import com.intellij.openapi.components.{PersistentStateComponent, Storage, State}
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * @author il
 */
@State(name = "IdeaColorSchemesSettings", storages = Array(new Storage(id = "default", file = "$APP_CONFIG$/" + configFolder +"/settings.xml")))
class IdeaSettings extends PersistentStateComponent[IdeaSettings] with UserSetting with BookSetting {
  def getState = this

  def loadState(state: IdeaSettings) {
    XmlSerializerUtil.copyBean(state, this)
  }
}
