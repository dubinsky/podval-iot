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

package org.podval.iot.app

import org.podval.iot.raspberrypi.RaspberryPi
import org.podval.iot.i2c.device.{Sht21, SevenSegment}

import java.util.Date


object Sensor {

  def main(args: Array[String]) {
    val pi = new RaspberryPi
    val sensor = new Sht21(pi.i2c)
    val sensorDisplay = new SevenSegment(pi.i2c)
    val timeDisplay = new SevenSegment(pi.i2c, 1)

    while (true) {
      val temperature = math.max(0, math.round(sensor.temperature))
      val humidity = math.max(0, math.round(sensor.humidity))

      sensorDisplay.left = temperature
      sensorDisplay.leftDot = true

      sensorDisplay.right = humidity
      sensorDisplay.rightDot = true

      sensorDisplay.update

      val date = new Date

      timeDisplay.left = date.getHours
      timeDisplay.right = date.getMinutes
      timeDisplay.colon = (date.getSeconds % 2) == 0
      timeDisplay.update

      Thread.sleep(1000)
    }
  }
}
