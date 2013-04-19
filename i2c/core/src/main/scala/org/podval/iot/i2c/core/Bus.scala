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

package org.podval.iot.i2c.core

import java.io.RandomAccessFile
import org.podval.iot.system.Fd


/*
 * XXX I can not figure out how to:
 * - nest Bus inside I2c so that it can do the close, but not anybody else
 * - nest Address within the Bus so that it can access file, but not anybody else
 * - define Bus and - more importantly, Address - in a separate file
 */
final class Bus(val i2c: I2c, val number: Int) {
  
  require(number >= 0, "Invalid bus number: " + number)

  
  val file: RandomAccessFile = new RandomAccessFile(busDevice, "rw")
  

  val fd = Fd.get(file)


  override def toString: String = "bus " + number + " (" + busDevice + ")"
  
  
  def busDevice: String = Bus.devicePrefix + number
  
  
  def close = {
    i2c.close(this)
    file.close
  }


  def address(value: Int): Address = new Address(this, value)


  def getFunctions: Long = I2c.getFunctions(fd)


  // XXX synchronize and reuse structures?

  def writeQuick(address: Int, data: Byte) = I2c.writeQuick(fd, address, data)

  def writeByte(address: Int, data: Byte) = I2c.writeByte(fd, address, data)
  def writeByte(address: Int, command: Byte, data: Byte) = I2c.writeByte(fd, address, command, data)
  def writeWord(address: Int, data: Short) = I2c.writeWord(fd, address, data)
  def writeWord(address: Int, command: Byte, data: Short) = I2c.writeWord(fd, address, command, data)
  def writeBytes(address: Int, data: Seq[Byte]) = I2c.writeBytes(fd, address, data)
  def writeBytes(address: Int, command: Byte, data: Seq[Byte]) = I2c.writeBytes(fd, address, command, data)

  def readByte(address: Int): Byte = I2c.readByte(fd, address)
  def readByte(address: Int, command: Byte): Byte = I2c.readByte(fd, address, command)
  def readWord(address: Int): Short = I2c.readWord(fd, address)
  def readWord(address: Int, command: Byte): Short = I2c.readWord(fd, address, command)
  // XXX switch to I2c.readBytes(fd, address, length) when it works...
  def readBytes(address: Int, length: Byte): Seq[Byte] = I2c.readBytes(fd, file, address, length)
  def readBytes(address: Int, command: Byte, length: Byte): Seq[Byte] = I2c.readBytes(fd, address, command, length)

  def setSlaveAddress(address: Int): Unit = I2c.setSlaveAddress(fd, address)
}


object Bus {
  
  val devicePrefix = "/dev/i2c-"
}
