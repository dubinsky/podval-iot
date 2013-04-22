/*
 * Copyright 2012-2013 Podval Group.
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

import org.podval.iot.platform.raspberrypi.RaspberryPi
import java.util.Date
import org.podval.iot.device.i2c.{Sht21, SevenSegment, Mpl115a2}


object Sensor {

  def main(args: Array[String]) {
    val pi = new RaspberryPi
    val bus = pi.i2c
    val sht21 = new Sht21(bus)
    val sht21Display = new SevenSegment(bus, 0)
    val mpl115a2 = new Mpl115a2(bus)
    val mpl115a2Display = new SevenSegment(bus, 1)

    val timeDisplay = new SevenSegment(pi.i2c, 2)

    while (true) {
      val temperature = math.max(0, math.round(sht21.temperature))
      val humidity = math.max(0, math.round(sht21.humidity))

      sht21Display.setLeft(temperature)
      sht21Display.setLeftDot(true)

      sht21Display.setRight(humidity)
      sht21Display.setRightDot(true)

      sht21Display.update

      val temperature2 = math.max(0, math.round(mpl115a2.temperature))
      mpl115a2Display.setLeft(temperature2)
      mpl115a2Display.setLeftDot(true)

      mpl115a2Display.update


      val date = new Date

      timeDisplay.setLeft(date.getHours)
      timeDisplay.setRight(date.getMinutes)
      timeDisplay.setColon((date.getSeconds % 2) == 0)
      timeDisplay.update

      Thread.sleep(1000)
    }
  }
}
