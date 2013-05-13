/*
 * Copyright 2013 Podval Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.podval.iot.cosm

import java.util.{Date, TimeZone}
import java.text.SimpleDateFormat


object Iso8601 {

  // XXX Allegedly, JDK 7 supports ISO 8601 better, but this still works, I hope...
  private[this] val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
  format.setTimeZone(TimeZone.getTimeZone("UTC"))


  def toString(date: Date) = format.format(date)
}
