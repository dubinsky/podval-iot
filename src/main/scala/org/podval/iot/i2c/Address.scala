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

import com.sun.jna.{Structure, Union}


// See i2c-dev.h from the i2c-tools package.

final class Address(val bus: Bus, val address: Int) {

  if (address < 0 || address > 0xff) throw new IllegalArgumentException("Invalid i2c address " + address)

  override def toString = "address " + address + " on " + bus


  def readByte: Byte = bus.readByte(address)
  def writeByte(data: Int) = bus.writeByte(address, data)
  def readShort: Short = bus.readShort(address)
  def writeShort(data: Int) = bus.writeShort(address, data)
  def writeByte(reg: Int, data: Int) = bus.writeByte(address, reg, data)
  def writeShort(reg: Int, data: Int) = bus.writeShort(address, reg, data)
  def readBytes(length: Int): Seq[Byte] = bus.readBytes(address, length)
  def writeBytes(data: Seq[Int]): Unit = bus.writeBytes(address, data)
  def writeBytes(reg: Int, data: Seq[Int]): Unit = bus.writeBytes(address, reg, data)
  def setSlaveAddress = bus.setSlaveAddress(address)
}
