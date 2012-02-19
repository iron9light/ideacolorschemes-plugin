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
