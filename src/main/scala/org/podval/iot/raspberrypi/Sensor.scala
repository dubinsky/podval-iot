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

package org.podval.iot.raspberrypi

import org.podval.iot.i2c.device.{Sht21, SevenSegment}


object Sensor {

  def main(args: Array[String]) {
    val pi = new RaspberryPi
    val sensor = new Sht21(pi.i2c)
    val display = new SevenSegment(pi.i2c)

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
}
