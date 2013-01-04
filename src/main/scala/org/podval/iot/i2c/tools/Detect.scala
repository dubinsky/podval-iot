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

package org.podval.iot.i2c.tools

import org.podval.iot.i2c.{I2c, Bus, Device}


object Detect {

  def scan(bus: Bus, mode: Device.Mode = Device.Default, first: Int = 0x03, last: Int = 0x77) {
    println("     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f")

    for (i <- 0 until 128 by 16) {
      val line = for (j <- 0 until 16) yield {
        val device = bus.device(i+j)

        /* Skip unwanted addresses */
        if (device.address < first || device.address > last)
          "  "
        else if (device.isPresent(mode))
          ("%02x" format device.address)
        else
          "--"
      }

      println(("%02X: " format i) + line.mkString(" "))
    }
  }


  def main(args: Array[String]) {
    scan((new I2c).bus(1))
  }
}
