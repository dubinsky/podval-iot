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

import org.podval.iot.i2c.{Register, Address}


/**
 * Inspired by (XXX link)  by Daniel Berlin and Limor Fried
 *
 * @param address
 */
// XXX Expose Gpio/Pin interface!
abstract class Mcp230xx(address: Address) {

  def numGpios: Int


  protected val direction: Register
  protected val pullUp: Register
  protected val output: Register
  protected val input: Register


  // Default: all pins are input, no pull-ups, output values read from the chip.
  direction.set(true)
  direction.write
  pullUp.set(false)
  pullUp.write
  output.load


  def setInput(pin: Int, value: Boolean) = {
    checkPin(pin)
    direction.set(pin, value)
    direction.writeForBit(pin)
  }


  def setPullUp(pin: Int, value: Boolean) {
    checkPin(pin)
    checkInput(pin, true)
    pullUp.set(pin, value)
    pullUp.writeForBit(pin)
  }


  def read(pin: Int): Boolean = {
    checkPin(pin)
    checkInput(pin, true)
    input.read(pin)
  }


  def write(pin: Int, value: Boolean) {
    checkPin(pin)
    checkInput(pin, false)
    output.setAndWriteIfChanged(pin, value)
  }


  private[this] def checkPin(pin: Int) =
    require(0 <= pin && pin < numGpios, "Invalid pin %s; must be from 0 to %s" format (numGpios-1))


  private[this] def checkInput(pin: Int, value: Boolean) =
    require(direction.get(pin) == value, "Pin %s not set to %s" format (pin, if (value) "input" else "output"))
}



final class Mcp23008(address: Address) extends Mcp230xx(address) {

  override def numGpios: Int = 8

  protected val direction = address.byteRegister(0x00)
  protected val pullUp = address.byteRegister(0x06)
  protected val output = address.byteRegister(0x0A)
  protected val input = address.byteRegister(0x09)
}



// XXX write Seq[Byte] to either of the GPIO registers...
final class Mcp23017(address: Address) extends Mcp230xx(address) {

  override def numGpios = 16


  protected val direction = address.wordRegister(0x00)
  protected val pullUp = address.wordRegister(0x0C)
  protected val output = address.wordRegister(0x14)
  protected val input = address.wordRegister(0x12)
}
