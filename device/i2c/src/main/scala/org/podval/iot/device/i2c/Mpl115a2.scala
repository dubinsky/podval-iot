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

package org.podval.iot.device.i2c

import org.podval.iot.i2c.Bus


/**
 * Barometric Pressure/Temperature Sensor from Adafruit.
 *
 * see https://github.com/adafruit/Adafruit_MPL115A2/blob/master/Adafruit_MPL115A2.cpp
 *
 * @param bus
 */
final class Mpl115a2(bus: Bus) {

  private[this] val address = bus.address(0x60)

  // Gets the factory-set coefficients for this particular sensor

  private[this] def readShort(register: Byte): Int = (address.readByte(register) << 8) | address.readByte((register+1).toByte)

  val a0_ : Int = readShort(Mpl115a2.a0Coeff)   // mkShort(coeffBytes(0), coeffBytes(1))
  val b1_ : Int = readShort(Mpl115a2.b1Coeff)   // mkShort(coeffBytes(2), coeffBytes(3))
  val b2_ : Int = readShort(Mpl115a2.b2Coeff)   // mkShort(coeffBytes(4), coeffBytes(5))
  val c12_ : Int = readShort(Mpl115a2.c12Coeff) >> 2 // (((coeffBytes(6) << 8) | coeffBytes(7)) >> 2)

  val a0 : Float = a0_ / 8.0f
  val b1 : Float = b1_ / 8192.0f
  val b2 : Float = b2_ / 16384.0f
  val c12: Float = c12 / 4194304.0f

  println("coefficients: " + a0 + " " + b1 + " " + b2 + " " + c12)

  // Pressure in kPa
  def pressure: Float = reading._1


  // Temperature in degrees Celsius
  def temperature: Float = reading._2


  def reading: (Float, Float) = {
    // Get raw pressure and temperature settings
    address.writeByte(Mpl115a2.startConversion, 0x00)

    // Wait a bit for the conversion to complete (3ms max)
    Thread.sleep(6)

    val pressure_    : Int = readShort(Mpl115a2.pressure   ) >> 6
    val temperature_ : Int = readShort(Mpl115a2.temperature) >> 6

    // See datasheet p.6 for evaluation sequence
    val pressureComp: Float = a0 + (b1 + c12*temperature_)*pressure_ + b2*temperature_
    // Pcomp will produce a value of 0 with an input pressure of 50 kPa and
    // will produce a full-scale value of 1023 with an input pressure of 115 kPa.

    // Return pressure and temperature as floating point values
    val pressure = ((115.0f - 50.0f) / 1023.0f) * pressureComp + 50.0f           // kPa   XXX 60.0f? (datasheet)
    val temperature = (temperature_.toFloat - 498.0f) / -5.35f + 25.0f            // C

    println("pressure= " + pressure + "kPa; temperature=" + temperature + "C")

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