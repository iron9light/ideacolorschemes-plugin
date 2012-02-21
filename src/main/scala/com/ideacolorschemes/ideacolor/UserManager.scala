package com.ideacolorschemes.ideacolor

import reflect.BeanProperty
import collection.mutable.Publisher
import util.IdeaUtil

/**
 * @author il
 * @version 11/11/11 10:54 AM
 */

trait UserManager {
  def userId: String

  def key: String
  
  def update(userId: String, key: String)
}

object UserManager extends UserManager with Publisher[String] with IdeaUtil {
  private[this] def userSettings: UserSetting = service[IdeaSettings]

  def userId = userSettings.userId

  private[this] def userId_=(value: String) {
    if (value != userSettings.userId) {
      userSettings.userId = value
      publish(value)
    }
  }

  def key = userSettings.key

  private[this] def key_=(value: String) {
    userSettings.key = value
  }

  def update(userId: String, key: String) {
    key_=(key)
    userId_=(userId)
  }
}

trait UserSetting {
  @BeanProperty
  var userId: String = ""
  @BeanProperty
  var key: String = ""
}

