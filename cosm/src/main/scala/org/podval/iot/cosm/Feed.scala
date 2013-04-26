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


final class Feed(cosm: Cosm, id: Int) {

  // create: POST to   http://api.cosm.com/v2/feeds
  //  Attribute	Description	Required?	User-settable

//  val title: String
//  val updated: String
//  val creator: String // URL
//  val feed: String // URL
//  val live: Boolean // status: "live" | "frozen"
//  val description: Option[String]
//  val website: Option[String] // URL
//  val icon: Option[String] // URL
//  val tags: Set[String]
//  val location: Option[Location]
//  val priv: Boolean
//  val user: String // owner

  //    Data	A datastream	No	Yes

  def getConnection(suffix: String) : HttpURLConnection = cosm.getConnection("/feeds/" + id + suffix)


  def getDatastream(id: Int): Datastream = new Datastream(this, id)
}
