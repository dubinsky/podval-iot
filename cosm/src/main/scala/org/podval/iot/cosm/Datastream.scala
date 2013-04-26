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

import java.util.Date
import java.net.HttpURLConnection
import java.io.{BufferedWriter, OutputStreamWriter}
import xml.Node


final class Datastream(feed: Feed, id: Int) {

  def getConnection(suffix: String): HttpURLConnection = feed.getConnection("/datastreams/" + id + suffix)


  def addDatapoint(timestamp: Date, value: Float): Unit = {
    val connection = getConnection("/datapoints/")
    connection.setRequestMethod("POST")
    connection.setDoInput(true)
    connection.setDoOutput(true)

    val data: Node =
      <eeml xmlns="http://www.eeml.org/xsd/0.5.1"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            version="0.5.1"
            xsi:schemaLocation="http://www.eeml.org/xsd/0.5.1 http://www.eeml.org/xsd/0.5.1/0.5.1.xsd">
        <environment>
          <data>
            <datapoints>
              <value at={timestamp.toString}>{value}</value>
            </datapoints>
          </data>
        </environment>
      </eeml>

    val os = connection.getOutputStream
    val writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))
    writer.write(data.toString)
    writer.close
    os.close

    connection.connect
  }
}
