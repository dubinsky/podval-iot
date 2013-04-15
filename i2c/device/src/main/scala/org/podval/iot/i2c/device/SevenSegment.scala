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

package org.podval.iot.i2c.device

import org.podval.iot.i2c.core.Bus


/**
 * Four-character, seven segment displays available from Adafruit.
 */
final class SevenSegment(bus: Bus, number: Int = 0) extends LedBackpack(bus, number) {

  def digit0_=(value: Int) = writeDigit(0, value)
  private[this] def digit0: Unit = {}

  def dot0_=(value: Boolean) = writeDot(0, value)
  private[this] def dot0: Unit = {}


  def digit1_=(value: Int) = writeDigit(1, value)
  private[this] def digit1: Unit = {}

  def dot1_=(value: Boolean) = writeDot(1, value)
  private[this] def dot1: Unit = {}


  def digit2_=(value: Int) = writeDigit(3, value)
  private[this] def digit2: Unit = {}

  def dot2_=(value: Boolean) = writeDot(3, value)
  private[this] def dot2: Unit = {}


  def digit3_=(value: Int) = writeDigit(4, value)
  private[this] def digit3: Unit = {}

  def dot3_=(value: Boolean) = writeDot(4, value)
  private[this] def dot3: Unit = {}


  def left_=(value: Int) = {
    digit0 = value / 10
    digit1 = value % 10
  }
  def left: Unit = {}


  def leftDot_=(value: Boolean) = dot1 = value
  def leftDot: Unit = {}


  def right_=(value: Int) = {
    digit2 = value / 10
    digit3 = value % 10
  }
  def right: Unit = {}


  def rightDot_=(value: Boolean) = dot3 = value
  def rightDot: Unit = {}


  // Sets a single decimal or hexademical value (0..9 and A..F)
  private[this] def writeDigit(charNumber: Int, value: Int) {
    if (value < 0 || value > 0xf) {
      throw new IllegalArgumentException("Invalid value: " + value)
    }

    // Set the appropriate digit
    buffer(charNumber) = SevenSegment.Digits(value)
  }

  
  private[this] def writeDot(charNumber: Int, value: Boolean) {
    buffer(charNumber) = (buffer(charNumber) & ~0x80) | (if (value) 0x80 else 0x00)
  }
    
  // 
  // Enables or disables the colon character
  // Warning: This function assumes that the colon is character '2',
  // which is the case on 4 char displays, but may need to be modified
  // if another display type is used
  def colon_=(value: Boolean = true) {
    buffer(2) = if (value) 0xffff else 0
  }
  def colon: Unit = {}
}


object SevenSegment {
  // Hexadecimal character lookup table (row 1 = 0..9, row 2 = A..F)
  val Digits: Array[Int] = Array(
    0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F,
    0x77, 0x7C, 0x39, 0x5E, 0x79, 0x71)
}
