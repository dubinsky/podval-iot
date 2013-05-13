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

import org.podval.iot.cosm.{Cosm, Datapoint}

import org.podval.iot.platform.raspberrypi.RaspberryPi
import org.podval.iot.device.i2c.{Ads1x15, Sht21, SevenSegment, Mpl115a2}

import org.kohsuke.args4j.{CmdLineParser, CmdLineException, Option => Arg}

import java.util.Date

import scala.collection.JavaConversions._


class Sensor {

  def run {
    val feed = new Cosm(cosmKey).getFeed(cosmFeed)
    val temperature2Datastream = feed.getDatastream(0)
    val temperatureDatastream = feed.getDatastream(1)
    val humidityDatastream = feed.getDatastream(2)

    val pi = new RaspberryPi
    val bus = pi.i2c

    val sht21 = new Sht21(bus)
    val sht21Display = new SevenSegment(bus, 0)

    val ads1015 = new Ads1x15(bus, 0)
    val ads1015Display = new SevenSegment(bus, 1)

//    val mpl115a2 = new Mpl115a2(bus)
//    val mpl115a2Display = new SevenSegment(bus, 3)

    val timeDisplay = new SevenSegment(bus, 2)

    while (true) {
      val now = new Date
      val seconds = now.getSeconds

      val temperature = sht21.temperature
      val humidity = sht21.humidity
      val temperature2 = Tmp36.toCelsius(ads1015.readADCSingleEnded(3))

      sht21Display.setLeft(toDisplay(temperature))
      sht21Display.setLeftDot(true)

      sht21Display.setRight(toDisplay(humidity))
      sht21Display.setRightDot(true)

      sht21Display.update

      ads1015Display.setLeft(toDisplay(temperature2))
      ads1015Display.setLeftDot(true)
      ads1015Display.update

      //val temperature2 = math.max(0, math.round(mpl115a2.temperature))
      //      mpl115a2Display.setLeft(temperature2) // conversions are so wrong that the "digit" is 21...
      //mpl115a2Display.setLeftDot(true)
      //mpl115a2Display.update

      timeDisplay.setLeft(now.getHours)
      timeDisplay.setRight(now.getMinutes)
      timeDisplay.setColon((seconds % 2) == 0)
      timeDisplay.update

      if ((seconds % 30) == 0) {
        temperatureDatastream.addDatapoint(Datapoint(now, toCosm(temperature)))
        temperature2Datastream.addDatapoint(Datapoint(now, toCosm(temperature2)))
        humidityDatastream.addDatapoint(Datapoint(now, toCosm(humidity)))
      }

      Thread.sleep(1000)
    }
  }


  def toCosm(value: Float): Float = math.round(value*10f)/10f


  def toDisplay(value: Float): Int = math.max(0, math.round(value))


  @Arg(name="--feed")
  private var cosmFeed: Int = _


  @Arg(name="--key")
  private var cosmKey: String = _
}


object Sensor {

  def main(args: Array[String]) {
    val app = new Sensor
    val parser = new CmdLineParser(app)
    try {
      parser.parseArgument(args.toSeq)
      app.run
    } catch {
      case e: CmdLineException =>
        // handling of wrong arguments
        System.err.println(e.getMessage)
        parser.printUsage(System.err)
    }
  }
}


object Tmp36 {

  // 750 mV at 25 degrees Celsius; 10 mV = 1 degree
  def toCelsius(millivolts: Int): Float = 25.0f + (millivolts - 750) / 10
}
