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

package org.podval.iot.i2c


final class Address(val bus: Bus, val address: Int) {

  require(0 <= address && address <= 0xff, "Invalid i2c address " + address)


  override def toString = "address " + address + " on " + bus


  def byteRegister(value: Byte): Register = new ByteRegister(this, value)
  def wordRegister(value: Byte): Register = new WordRegister(this, value)


  def writeQuick(data: Byte) = bus.writeQuick(address, data)

  def writeByte(data: Byte) = bus.writeByte(address, data)
  def writeByte(command: Byte, data: Byte) = bus.writeByte(address, command, data)
  def writeWord(data: Short) = bus.writeWord(address, data)
  def writeWord(command: Byte, data: Short) = bus.writeWord(address, command, data)
  def writeBytes(data: Seq[Byte]) = bus.writeBytes(address, data)
  def writeBytes(command: Byte, data: Seq[Byte]) = bus.writeBytes(address, command, data)

  def readByte: Byte = bus.readByte(address)
  def readByte(command: Byte): Byte = bus.readByte(address, command)
  def readWord: Short = bus.readWord(address)
  def readWord(command: Byte): Short = bus.readWord(address, command)
  def readBytes(length: Byte): Seq[Byte] = bus.readBytes(address, length)
  def readBytes(command: Byte, length: Byte): Seq[Byte] = bus.readBytes(address, command, length)

  def setSlaveAddress: Unit = bus.setSlaveAddress(address)
}
