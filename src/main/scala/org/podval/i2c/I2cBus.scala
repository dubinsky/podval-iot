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

import java.io.RandomAccessFile

import Ioctl.toIoctl


final class I2cBus(bus: Int) {

  if (bus < 0) {
    throw new IllegalArgumentException("Invalid bus number: " + bus)
  }


  private val file: RandomAccessFile = new RandomAccessFile(I2cBus.busDevice(bus), "rw")


  def close = file.close


  override def toString: String = "i2c bus " + bus + " on " + I2cBus.busDevice(bus)


  def writeByte(data: Int) = file.write(data.asInstanceOf[Byte])


  def writeBytes(data: Seq[Int]) = file.write(data.map(_.asInstanceOf[Byte]).toArray)


  def readByte = file.readByte


  def readBytes(length: Int): Seq[Byte] = {
    val buffer: Array[Byte] = new Array(length)
    val result = file.read(buffer)
    // XXX: when length is right, there is no copying? Right?!
    buffer.take(math.max(0, result))
  }


  def setSlaveAddress(address: Int) {
    I2cBus.checkAddress(address)

    val result = file.ioctl(I2cBus.SET_SLAVE_ADDRESS, address)

    val ok = result >= 0

    if (!ok) {
      throw new NoSuchElementException("No device at address " + address + " on " + this)
    }
  }


  def isPresent(address: Int): Boolean = {
    setSlaveAddress(address)
    val bytes = readBytes(address)
    !bytes.isEmpty
  }
}



object I2cBus {

  val busDevicePrefix = "/dev/i2c-"


  val SET_SLAVE_ADDRESS = 0x0703


  def busDevice(bus: Int): String = busDevicePrefix + bus


  def checkAddress(address: Int) {
    if (address < 0 || address > 0xff) throw new IllegalArgumentException("Invalid i2c address " + address)
  }
}
