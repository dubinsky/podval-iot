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

import java.io.{IOException, RandomAccessFile}
import org.podval.iot.system.{Fd, CLib}


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

  def writeQuick(data: Int) = I2c.writeQuick(fd, data)
  def readByte(address: Int): Byte = I2c.readByte(file, address)
  def writeByte(address: Int, data: Int) = I2c.writeByte(file, address, data)
  def writeByte(data: Int) = I2c.writeByte(fd, data)
  def writeByteData(command: Int, data: Int) = I2c.writeByteData(fd, command, data)
  def readShort(address: Int): Short = I2c.readShort(file, address)
  def writeShort(address: Int, data: Int) = I2c.writeShort(file, address, data)
  def writeWordData(command: Int, data: Int) = I2c.writeWordData(fd, command, data)
  //  def readByte(reg: Int): Byte = I2c.readByte(fd, reg)
  def writeByte(address: Int, reg: Int, data: Int) = I2c.writeByte(file, address, data)
  //  def readShort(reg: Int): Byte = I2c.readShort(fd, reg)
  def writeShort(address: Int, reg: Int, data: Int) = I2c.writeShort(file, address, reg, data)
  def readBytes(address: Int, length: Int): Seq[Byte] = I2c.readBytes(file, address, length)
  def writeBytes(address: Int, data: Seq[Int]): Unit = I2c.writeBytes(file, address, data)
  //  def readBytes(reg: Int, length: Int): Seq[Byte] = I2c.readBytes(fd, reg, length)
  def writeBytes(address: Int, reg: Int, data: Seq[Int]): Unit = I2c.writeBytes(file, address, reg +: data)

  def setSlaveAddress(address: Int): Unit = I2c.setSlaveAddress(file, address)
}


object Bus {
  
  val devicePrefix = "/dev/i2c-"
}
