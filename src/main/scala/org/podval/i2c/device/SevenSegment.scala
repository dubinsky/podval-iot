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

package org.podval.i2c.device

import org.podval.i2c.{I2cBus, I2cDevice}


/**
 * Four-character, seven segment displays available from Adafruit.
 */
// XXX: Encapsulate the underlying read/write?
final class SevenSegment(bus: I2cBus, number: Int = 0) extends LedBackpack(bus, number) {

  // Sets a single decimal or hexademical value (0..9 and A..F)
  def writeDigit(charNumber: Int, value: Int, dot: Boolean = false) {
    if (charNumber < 0 || charNumber > 7) {
      throw new IllegalArgumentException("Invalid character number: " + charNumber)
    }

    if (value < 0 || value > 0xf) {
      throw new IllegalArgumentException("Invalid value: " + value)
    }

    // Set the appropriate digit
    buffer(charNumber) = SevenSegment.DIGITS(value) | (if (dot) 0x80 else 0x00)

    // Update
    update // XXX
  }


  // Enables or disables the colon character
  // Warning: This function assumes that the colon is character '2',
  // which is the case on 4 char displays, but may need to be modified
  // if another display type is used
  def setColon(value: Boolean = true) {
    buffer(2) = if (value) 0xffff else 0
    update
  }
}


object SevenSegment {
  // Hexadecimal character lookup table (row 1 = 0..9, row 2 = A..F)
  val DIGITS: Array[Int] = Array(
    0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F,
    0x77, 0x7C, 0x39, 0x5E, 0x79, 0x71)
}
