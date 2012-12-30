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

package org.podval.i2c


class I2cDevice(bus: I2cBus, address: Int) {

  I2cBus.checkAddress(address)


  final def writeByte(data: Int) {
    bus.setSlaveAddress(address)
    writeBytes(Seq(data))
  }


  final def writeByte(reg: Int, data: Int) = writeBytes(Seq(reg, data))


  final def writeBytes(data: Seq[Int]) {
    bus.setSlaveAddress(address)
    bus.writeBytes(data)
  }


  final def readByte: Byte = {
    bus.setSlaveAddress(address)
    bus.readByte
  }


  final def readBytes(length: Int): Seq[Byte] = {
    bus.setSlaveAddress(address)
    bus.readBytes(length)
  }
}
