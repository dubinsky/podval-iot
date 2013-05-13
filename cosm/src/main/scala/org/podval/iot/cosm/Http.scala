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

import java.net.HttpURLConnection
import java.io.{InputStreamReader, BufferedReader, OutputStreamWriter, BufferedWriter}


object Http {

  def post(connection: HttpURLConnection, data: String) {
    connection.setRequestMethod("POST")
    connection.setDoInput(true)
    connection.setDoOutput(true)

    val os = connection.getOutputStream
    val writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))
    writer.write(data)
    writer.close
    os.close

    connection.connect

    // XXX I do not care for the response, but unless I do the following, request does not get to the server?
    val is = connection.getInputStream
    val reader = new BufferedReader(new InputStreamReader(is))
    var done = false
    while (!done) {
      val line = reader.readLine
      done = (line == null)
      if (!done) println("response: " + line)
    }
    is.close

    connection.disconnect
  }
}
