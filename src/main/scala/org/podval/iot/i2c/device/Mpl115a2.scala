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

package org.podval.iot.i2c.device

import org.podval.iot.i2c.Bus


/**
 * Barometric Pressure/Temperature Sensor from Adafruit.
 *
 * @param bus
 */
// XXX conversions are wrong!!!
final class Mpl115a2(bus: Bus) {

  val address = bus.address(0x60)

  // Gets the factory-set coefficients for this particular sensor
  address.writeByte(Mpl115a2.a0Coeff)

  // device.request(8)// XXX
  val a0 : Float = address.readShort.toFloat / 8
  val b1 : Float = address.readShort.toFloat / 8192
  val b2 : Float = address.readShort.toFloat / 16384
  val c12: Float = (((address.readByte.toShort << 8) | (address.readByte >> 2)).toFloat / 4194304.0).toFloat


  // Pressure in kPa
  def pressure: Float = reading._1


  // Temperature in degrees Celsius
  def temperature: Float = reading._2


  def reading: (Float, Float) = {
    // Get raw pressure and temperature settings
    address.writeByteData(Mpl115a2.startConversion, 0x00)

    // Wait a bit for the conversion to complete (3ms max)
    Thread.sleep(5)

    address.writeByte(Mpl115a2.pressure)

    def readMeasurement: Int = (address.readByte.toShort << 8) | (address.readByte >> 6)

    // device.request(4)// XXX
    val pressureRaw = readMeasurement
    val temperatureRaw = readMeasurement

    // See datasheet p.6 for evaluation sequence
    val pressureComp: Float = a0 + (b1 + c12*temperatureRaw)*pressureRaw + b2*temperatureRaw

    // Return pressure and temperature as floating point values
    val pressure = ((65.0F / 1023.0F) * pressureComp) + 50.0F           // kPa
    val temperature = (temperatureRaw.toFloat - 498.0F) / -5.35F +25.0F // C

    (pressure, temperature)
  }
}


object Mpl115a2 {

  val address: Int = 0x60

  // Registers
  val pressure       : Byte = 0x00 // Msb; Lsb
  val temperature    : Byte = 0x02 // Msb; Lsb
  val a0Coeff        : Byte = 0x04 // Msb; Lsb
  val b1Coeff        : Byte = 0x06 // Msb; Lsb
  val b2Coeff        : Byte = 0x08 // Msb; Lsb
  val c12Coeff       : Byte = 0x0A // Msb; Lsb
  val startConversion: Byte = 0x12
}