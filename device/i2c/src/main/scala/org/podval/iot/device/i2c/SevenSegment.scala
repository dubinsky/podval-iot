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

package org.podval.iot.device.i2c

import org.podval.iot.i2c.Bus


/**
 * Four-character, seven segment displays available from Adafruit.
 */
final class SevenSegment(bus: Bus, number: Int = 0) {

  private[this] val backpack = new LedBackpack(bus, number)


  def setDigit(charNumber: Int, value: Int) = setCode(charNumber, SevenSegment.getDigitCode(value))


  def setDot(charNumber: Int, value: Boolean) = backpack.setBit(correctNumber(charNumber), 0x80.toByte, value)


  // Enables or disables the colon character
  // Warning: This function assumes that the colon is character '2',
  // which is the case on 4 char displays, but may need to be modified
  // if another display type is used
  def setColon(value: Boolean = true) = backpack.setWord(SevenSegment.right, (if (value) 0xffff else 0).toShort)


  def setCode(charNumber: Int, value: Byte) = backpack.setByte(correctNumber(charNumber),  value)


  private[this] def correctNumber(charNumber: Int): Int = {
    require(0 <= charNumber && charNumber <= 3, "Invalid digit number " + charNumber)
    if (charNumber < SevenSegment.right) charNumber else charNumber+1
  }


  def setLeft(value: Int) = setHalf(SevenSegment.left, value)
  def setRight(value: Int) = setHalf(SevenSegment.right, value)
  def setLeftDot(value: Boolean) = setDot(SevenSegment.left+1, value)
  def setRightDot(value: Boolean) = setDot(SevenSegment.right+1, value)


  private[this] def setHalf(digit: Int, value: Int) = {
    setDigit(digit  , value / 10)
    setDigit(digit+1, value % 10)
  }


  def update = backpack.update
}


object SevenSegment {
  private val left = 0
  private val right = 2


  def getDigitCode(digit: Int): Byte = {
    require(0 <= digit && digit <= 0x0f, "Invalid value: " + digit)
    SevenSegment.Digits(digit).toByte
  }

  // Hexadecimal character lookup table (row 1 = 0..9, row 2 = A..F)
  val Digits: Array[Int] = Array(
    0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F,
    0x77, 0x7C, 0x39, 0x5E, 0x79, 0x71)
}
