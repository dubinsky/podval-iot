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

  def writeQuick(address: Int, data: Int) = I2c.writeQuick(fd, address, data)
  def readByte(address: Int): Int = I2c.readByte(fd, address)
  def writeByte(address: Int, data: Int) = I2c.writeByte(fd, address, data)
  def readByteData(address: Int, command: Int): Int = I2c.readByteData(fd, address, command)
  def writeByteData(address: Int, command: Int, data: Int) = I2c.writeByteData(fd, address, command, data)
  def readWordData(address: Int, command: Int): Int = I2c.readWordData(fd, address, command)
  def writeWordData(address: Int, command: Int, data: Int) = I2c.writeWordData(fd, address, command, data)
  def processCall(address: Int, command: Int, data: Int): Int = I2c.processCall(fd, address, command, data)
  def readBlockData(address: Int, command: Int): Seq[Byte] = I2c.readBlockData(fd, address, command)
  def writeBlockData(address: Int, command: Int, data: Seq[Int]) = I2c.writeBlockData(fd, address, command, data)

  def setSlaveAddress(address: Int): Unit = I2c.setSlaveAddress(fd, address)

  def readByteSimple(address: Int): Byte = I2c.readByteSimple(file, address)
  def writeByteSimple(address: Int, data: Int) = I2c.writeByteSimple(file, address, data)
  def readShort(address: Int): Short = I2c.readShort(file, address)
  def writeShort(address: Int, data: Int) = I2c.writeShort(file, address, data)
  def writeByteSimple(address: Int, reg: Int, data: Int) = I2c.writeByteSimple(file, address, reg, data)
  def writeShort(address: Int, reg: Int, data: Int) = I2c.writeShort(file, address, reg, data)
  def readBytes(address: Int, length: Int): Seq[Byte] = I2c.readBytes(file, address, length)
  def writeBytes(address: Int, data: Seq[Int]): Unit = I2c.writeBytes(file, address, data)
  def writeBytes(address: Int, reg: Int, data: Seq[Int]): Unit = I2c.writeBytes(file, address, reg, data)
}


object Bus {
  
  val devicePrefix = "/dev/i2c-"
}
