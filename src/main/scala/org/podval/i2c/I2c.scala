/*
 * Copyright 2012 Podval Group.
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

package org.podval.i2c

import scala.collection.mutable


// XXX Introduce Controller class
// XXX scope Bus and Device inside; tighten accessibility of vals
object I2c {

  val busDevicePrefix = "/dev/i2c-"


  def bus(number: Int): Bus = {
    if (number2bus.get(number).isEmpty) {
      number2bus.put(number, new Bus(number))
    }

    number2bus(number)
  }


  def close(bus: Bus) {
    number2bus.remove(bus.number)
  }


  private val number2bus = mutable.Map[Int, Bus]()
}
