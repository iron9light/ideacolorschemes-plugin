package com.ideacolorschemes.ideacolor

import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.openapi.components.{ServiceManager, Storage, State, PersistentStateComponent}
import reflect.BeanProperty

/**
 * @author il
 * @version 11/11/11 10:54 AM
 */

trait UserManager {
  def userId: String

  def key: String
}

object UserManager extends UserManager {
  private[ideacolor] def userSettings = ServiceManager.getService(classOf[UserSettings])

  def userId = userSettings.userId

  def key = userSettings.key
}

@State(name = "IdeaColorSchemesSettings", storages = Array(new Storage(id = "default", file = "$APP_CONFIG$/ideacolorschemes_settings.xml")))
class UserSettings extends PersistentStateComponent[UserSettings] {
  @BeanProperty
  var userId: String = ""
  @BeanProperty
  var key: String = ""

  def getState = this

  def loadState(state: UserSettings) {
    XmlSerializerUtil.copyBean(state, this)
  }
}

