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

import java.net.{URL, HttpURLConnection}


final class Cosm(key: String) {

  def getConnection(suffix: String): HttpURLConnection = {
    val result = new URL("http://api.cosm.com/v2" + suffix).openConnection.asInstanceOf[HttpURLConnection]
    result.addRequestProperty("X-ApiKey", key)
    result
  }


  def getFeed(id: Int): Feed = new Feed(this, id)
}
