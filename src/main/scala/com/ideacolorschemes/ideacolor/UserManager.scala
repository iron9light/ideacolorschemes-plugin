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

