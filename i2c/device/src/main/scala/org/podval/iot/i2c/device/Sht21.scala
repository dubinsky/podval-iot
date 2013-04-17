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

package org.podval.iot.i2c.device

import org.podval.iot.i2c.core.Bus


final class Sht21(bus: Bus) {

  val address = bus.address(0x40)


  def temperature: Float = convertTemperature(readMeasurement(0xf3, false))
  

  def temperatureHold: Float = convertTemperature(readMeasurement(0xe3, true))
  

  def humidity: Float = convertHumidity(readMeasurement(0xf5, false))
  

  def humidityHold: Float = convertHumidity(readMeasurement(0xe5, true))


  private[this] def convertTemperature(value: Int) = convert(value, -46.85f, 175.72f)


  private[this] def convertHumidity(value: Int) = convert(value, -6.0f, 125.0f)


  private[this] def convert(value: Int, a: Float, b: Float): Float = a + b * value / 65535.0f


  private[this] def readMeasurement(command: Int, hold: Boolean) = {
    address.writeByte(command.toByte)
    
    if (hold == false) {
      // wait for conversion, 14 bits = 85ms
      Thread.sleep(85)
    }
    
    val bytes = address.readBytes(3) // XXX: readBlockDataI2c(0, 3)
    println("***** measurement " + bytes.mkString(","))
    // XXX: Signal failed read better!
    if (bytes.isEmpty) 0 else {
      val result = (bytes(0) << 8) | (bytes(1) & 0xfc)
      // val checksum = bytes(2)
      // val stat = bytes(1) & 0x3
      // check the sum
      result
    }
  }

  
//  def readUserRegister = {
//    address.writeByte(0xe7.toByte)
//    readByte
//  }
}
