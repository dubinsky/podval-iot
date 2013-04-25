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

import org.podval.iot.gpio.{Gpio, RegisterGpio}
import org.podval.iot.i2c.{Address, ByteRegisterI2c, WordRegisterI2c}


/**
 * Inspired by (XXX link)  by Daniel Berlin and Limor Fried
 *
 * @param address
 */
final class Mcp23008(address: Address) {

  val direction = new ByteRegisterI2c(address, 0x00)
  val pullUp    = new ByteRegisterI2c(address, 0x06)
  val output    = new ByteRegisterI2c(address, 0x0A)
  val input     = new ByteRegisterI2c(address, 0x09)


  val gpio: Gpio = new RegisterGpio(8, direction, pullUp, output, input)
}



// XXX write Seq[Byte] to either of the GPIO registers...
final class Mcp23017(address: Address) {

  val direction = new WordRegisterI2c(address, 0x00)
  val pullUp    = new WordRegisterI2c(address, 0x0C)
  val output    = new WordRegisterI2c(address, 0x14)
  val input     = new WordRegisterI2c(address, 0x12)


  val gpio: Gpio = new RegisterGpio(8, direction, pullUp, output, input)
}
