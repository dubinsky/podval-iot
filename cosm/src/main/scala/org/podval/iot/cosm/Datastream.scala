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


final class Datastream(feed: Feed, id: Int) {

  def getConnection(suffix: String): HttpURLConnection = feed.getConnection("/datastreams/" + id + suffix)


  // XXX I failed to make Cosm understand XML format of requests, and settled for JSON -
  // especially since the only thing I need right now is to be able to post a datapoint :)
  def addDatapoint(datapoint: Datapoint): Unit = {
//    val data: Node =
//      <eeml xmlns="http://www.eeml.org/xsd/0.5.1"
//            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//            version="0.5.1"
//            xsi:schemaLocation="http://www.eeml.org/xsd/0.5.1 http://www.eeml.org/xsd/0.5.1/0.5.1.xsd">
//        <environment>
//          <data>
//            <datapoints>
//              <value at={Iso8601,toString(datapoint.timestamp)}>{datapoint.value}</value>
//            </datapoints>
//          </data>
//        </environment>
//      </eeml>

    val data: String =
      "{\n" +
      "  \"datapoints\":[\n" +
      "    {\"at\":\"" + Iso8601.toString(datapoint.timestamp) + "\",\"value\":\"" + datapoint.value + "\"}\n" +
      "  ]\n" +
      "}\n"

//    println(data)

    Http.post(getConnection("/datapoints/"), data)
  }
}
