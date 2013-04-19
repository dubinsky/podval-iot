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

import org.podval.iot.i2c.core.Bus


/**
 * Barometric Pressure/Temperature Sensor from Adafruit.
 *
 * see https://github.com/adafruit/Adafruit_MPL115A2/blob/master/Adafruit_MPL115A2.cpp
 *
 * @param bus
 */
// XXX Does not work!!!
final class Mpl115a2(bus: Bus) {

  private[this] val address = bus.address(0x60)

  // Gets the factory-set coefficients for this particular sensor
  address.writeByte(Mpl115a2.a0Coeff)
  private[this] val coeffBytes = address.readBytes(8)
  private[this] def mkFloat(msb: Byte, lsb: Byte): Float = ((msb << 8) | lsb).toFloat
  val a0 : Float = mkFloat(coeffBytes(0), coeffBytes(1)) / 8.0f
  val b1 : Float = mkFloat(coeffBytes(2), coeffBytes(3)) / 8192.0f
  val b2 : Float = mkFloat(coeffBytes(4), coeffBytes(5)) / 16384.0f
  val c12: Float = mkFloat(coeffBytes(6), (coeffBytes(7) >> 2).toByte) / 4194304.0f
  println("coefficients: " + a0 + " " + b1 + " " + b2 + " " + c12)

  // Pressure in kPa
  def pressure: Float = reading._1


  // Temperature in degrees Celsius
  def temperature: Float = reading._2


  def reading: (Float, Float) = {
    // Get raw pressure and temperature settings
    address.writeByte(Mpl115a2.startConversion, 0x00)

    // Wait a bit for the conversion to complete (3ms max)
    Thread.sleep(5)

    address.writeByte(Mpl115a2.pressure)

    def mk12bits(msb: Byte, lsb: Byte): Short = (((msb << 8) | lsb) >> 6).toShort

    val bytes = address.readBytes(4)
    println("bytes " + bytes.mkString(","))
    val pressureRaw = mk12bits(bytes(0), bytes(1))
    val temperatureRaw = mk12bits(bytes(2), bytes(3))

    // See datasheet p.6 for evaluation sequence
    val pressureComp: Float = a0 + (b1 + c12*temperatureRaw)*pressureRaw + b2*temperatureRaw

    // Return pressure and temperature as floating point values
    val pressure = ((65.0f / 1023.0f) * pressureComp) + 50.0f           // kPa
    val temperature = (temperatureRaw.toFloat - 498.0f) / -5.35f +25.0f // C

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