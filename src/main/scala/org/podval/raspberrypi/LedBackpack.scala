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


/**
 * Adafruit's HT16K33-base LED backpack.
 */
// XXX: Encapsulate the underlying read/write?
class LedBackpack(bus: I2cBus, number: Int) extends I2cDevice(bus, 0x70 + number) {

  if (number < 0 || number > 7) {
    throw new IllegalArgumentException("Invalid LED backpack address: " + number)
  }

  val buffer = new Array[Int](8)

  // Turn the oscillator on
  write(Seq(LedBackpack.SYSTEM_SETUP_REGISTER | 0x01, 0x00))

  // Turn blink off
  setBlinkRate(LedBackpack.BlinkOff)

  // Set maximum brightness
  setBrightness(15)

  // Clear the screen
  clear
  update


  def clear {
//    buffer.fill(0)

    for (i <- 0 until buffer.length) {
      buffer(i) = 0x00
    }
  }


  def update {
    val bytes = new Array[Int](1+buffer.length*2)
    bytes(0) = 0x00
    for (i <- 0 until buffer.length) {
      bytes(1+i*2) = buffer(i) & 0xff
      bytes(2+i*2) = (buffer(i) >> 8) & 0xff
    }

    write(bytes)
  }

  def setBrightness(value: Int) {
    if (value < 0 || value > 15) {
      throw new IllegalArgumentException("Brightness must be between 0 and 15, not " + value)
    }

    write(Seq(LedBackpack.DIMMING_REGISTER | value, 0x00 ))
  }


  def setBlinkRate(value: LedBackpack.BlinkRate) {
    val rate = value match {
      case LedBackpack.BlinkOff => 0x00
      case LedBackpack.Blink2Hz => 0x01
      case LedBackpack.Blink1Hz => 0x02
      case LedBackpack.BlinkHalfHz => 0x03
    }

    write(Seq(LedBackpack.DISPLAY_SETUP_REGISTER | 0x01 | (rate << 1), 0x00))
  }
}


object LedBackpack {
  
  private val SYSTEM_SETUP_REGISTER : Int = 0x20
  private val DIMMING_REGISTER      : Int = 0xe0
  private val DISPLAY_SETUP_REGISTER: Int = 0x80

  sealed trait BlinkRate
  case object BlinkOff extends BlinkRate
  case object Blink1Hz extends BlinkRate
  case object Blink2Hz extends BlinkRate
  case object BlinkHalfHz extends BlinkRate
}
