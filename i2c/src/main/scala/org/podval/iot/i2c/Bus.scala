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

package org.podval.iot.i2c

import java.io.RandomAccessFile
import org.podval.iot.system.Fd


/*
 * XXX I can not figure out how to:
 * - nest Bus inside I2c so that it can do the close, but not anybody else
 * - nest Address within the Bus so that it can access file, but not anybody else
 * - define Bus and - more importantly, Address - in a separate file
 */
final class Bus(val i2c: I2c, val number: Int) {
  
  if (number < 0) {
    throw new IllegalArgumentException("Invalid bus number: " + number)
  }
  
  
  val file: RandomAccessFile = new RandomAccessFile(busDevice, "rw")
  

  val fd = Fd.get(file)


  override def toString: String = "bus " + number + " (" + busDevice + ")"
  
  
  def busDevice: String = Bus.devicePrefix + number
  
  
  def close = {
    i2c.close(this)
    file.close
  }


  def address(value: Int): Address = new Address(this, value)


  // XXX synchronize and reuse structures?

  def writeQuick(address: Int, data: Byte) = I2c.writeQuick(fd, address, data)
  def readByte(address: Int): Byte = I2c.readByte(fd, address)
  def writeByte(address: Int, data: Byte) = I2c.writeByte(fd, address, data)
  def readByteData(address: Int, command: Byte): Byte = I2c.readByteData(fd, address, command)
  def writeByteData(address: Int, command: Byte, data: Byte) = I2c.writeByteData(fd, address, command, data)
  def readWordData(address: Int, command: Byte): Short = I2c.readWordData(fd, address, command)
  def writeWordData(address: Int, command: Byte, data: Short) = I2c.writeWordData(fd, address, command, data)
  def processCall(address: Int, command: Byte, data: Short): Short = I2c.processCall(fd, address, command, data)
  def readBlockData(address: Int, command: Byte): Seq[Byte] = I2c.readBlockData(fd, address, command)
  def writeBlockData(address: Int, command: Byte, data: Seq[Byte]) = I2c.writeBlockData(fd, address, command, data)
  def readBlockDataI2c(address: Int, command: Byte, length: Byte): Seq[Byte] = I2c.readBlockDataI2c(fd, address, command, length);
  def writeBlockDataI2c(address: Int, command: Byte, data: Seq[Byte]) = I2c.writeBlockDataI2c(fd, address, command, data)
  def blockProcessCall(address: Int, command: Byte, data: Seq[Byte]) = I2c.blockProcessCall(fd, address, command, data)

//  def readByteSimple(address: Int): Byte = I2c.readByteSimple(fd, file, address)
//  def writeByteSimple(address: Int, data: Byte) = I2c.writeByteSimple(fd, file, address, data)
  def readShort(address: Int): Short = I2c.readShort(fd, file, address)
//  def writeShort(address: Int, data: Short) = I2c.writeShort(fd, file, address, data)
//  def writeByteSimple(address: Int, register: Byte, data: Byte) = I2c.writeByteSimple(fd, file, address, register, data)
//  def writeShort(address: Int, register: Byte, data: Short) = I2c.writeShort(fd, file, address, register, data)
//  def writeBytes(address: Int, register: Byte, data: Seq[Byte]): Unit = I2c.writeBytes(fd, file, address, register, data)
  def writeBytes(address: Int, data: Seq[Byte]): Unit = I2c.writeBytes(fd, file, address, data)
  def readBytes(address: Int, length: Int): Seq[Byte] = I2c.readBytes(fd, file, address, length)

  def setSlaveAddress(address: Int): Unit = I2c.setSlaveAddress(fd, address)
}


object Bus {
  
  val devicePrefix = "/dev/i2c-"
}
