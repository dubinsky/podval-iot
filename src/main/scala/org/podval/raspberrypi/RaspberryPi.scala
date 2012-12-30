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

package org.podval.raspberrypi

import org.podval.i2c.I2c

import org.podval.i2c.device.{Sht21, SevenSegment}


object RaspberryPi {

  lazy val revision: Int = {
    // XXX: revision is available from /proc/cpuinfo after keyword "Revision"
    2
  }


  val i2c0 = I2c.bus(0)


  val i2c1 = I2c.bus(1)


  val i2c = if (revision > 1) i2c1 else i2c0


  def main(args: Array[String]) {
    val sensor = new Sht21(i2c)
    val display = new SevenSegment(i2c)

    while (true) {
      val temperature = math.max(0, math.round(sensor.temperature))
      val humidity = math.max(0, math.round(sensor.humidity))

      println("temperature=" + temperature + " humidity=" + humidity)

      display.writeDigit(0, temperature / 10)
      display.writeDigit(1, temperature % 10, true)

      display.writeDigit(3, humidity / 10)
      display.writeDigit(4, humidity % 10, true)

      display.update

      Thread.sleep(1000)
    }
  }


  def detect {
    println((0x00 to 0x77).filter(i2c.device(_).isPresent))
  }
}
