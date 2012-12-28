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


final class Sht21(bus: I2cBus) extends I2cDevice(bus, 0x40) {

  def temperature: Float = convertTemperature(readMeasurement(0xf3, false))
  

  def temperatureHold: Float = convertTemperature(readMeasurement(0xe3, true))
  

  def humidity: Float = convertHumidity(readMeasurement(0xf5, false))
  

  def humidityHold: Float = convertHumidity(readMeasurement(0xe5, true))


  def convertTemperature(value: Int) = convert(value, -46.85f, 175.72f)


  def convertHumidity(value: Int) = convert(value, -6.0f, 125.0f)

  
  def convert(value: Int, a: Float, b: Float): Float = a + b * value / 65535.0f

  
  def readMeasurement(command: Int, hold: Boolean) = {
    val status = write(Seq(command))
    
    if (hold == false) {
      // wait for conversion, 14 bits = 85ms
      Thread.sleep(85)
    }
    
    val bytes = read(3)
    if (bytes.isEmpty) 0 else {
      val result = (bytes(0) << 8) | (bytes(1) & 0xfc)
      // val checksum = bytes(2)
      // val stat = bytes(1) & 0x3
      // check the sum
      result
    }
  }

  
  def readUserRegister = {
    write(Seq(0xe7))
    read(1)(0)
  }
}
