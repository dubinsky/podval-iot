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

package org.podval.iot.i2c.core


final class Address(val bus: Bus, val address: Int) {

  if (address < 0 || address > 0xff) throw new IllegalArgumentException("Invalid i2c address " + address)


  override def toString = "address " + address + " on " + bus


  def register(value: Byte): Register = new Register(this, value)


  def writeQuick(data: Byte) = bus.writeQuick(address, data)
  def readByte: Byte = bus.readByte(address)
  def writeByte(data: Byte) = bus.writeByte(address, data)
  def readByteData(command: Byte): Byte = bus.readByteData(address, command)
  def writeByteData(command: Byte, data: Byte) = bus.writeByteData(address, command, data)
  def readWordData(command: Byte): Short = bus.readWordData(address, command)
  def writeWordData(command: Byte, data: Short) = bus.writeWordData(address, command, data)
  def processCall(command: Byte, data: Short): Short = bus.processCall(address, command, data)
  def readBlockData(command: Byte): Seq[Byte] = bus.readBlockData(address, command)
  def writeBlockData(command: Byte, data: Seq[Byte]) = bus.writeBlockData(address, command, data)
  def readBlockDataI2c(command: Byte, length: Byte): Seq[Byte] = bus.readBlockDataI2c(address, command, length);
  def writeBlockDataI2c(command: Byte, data: Seq[Byte]) = bus.writeBlockDataI2c(address, command, data)
  def blockProcessCall(command: Byte, data: Seq[Byte]) = bus.blockProcessCall(address, command, data)

//  def readByteSimple: Byte = bus.readByteSimple(address)
//  def writeByteSimple(data: Byte): Unit = bus.writeByteSimple(address, data)
  def readShort: Short = bus.readShort(address)
//  def writeShort(data: Short): Unit = bus.writeShort(address, data)
//  def writeByteSimple(register: Byte, data: Byte) = bus.writeByteSimple(address, register, data)
//  def writeShort(register: Byte, data: Short) = bus.writeShort(address, register, data)
//  def writeBytes(register: Byte, data: Seq[Byte]): Unit = bus.writeBytes(address, register, data)
  def writeBytes(data: Seq[Byte]): Unit = bus.writeBytes(address, data)
  def readBytes(length: Int): Seq[Byte] = bus.readBytes(address, length)

  def setSlaveAddress: Unit = bus.setSlaveAddress(address)
}
