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

import Ioctl.toIoctl


final class Device(val bus: Bus, val address: Int) {

  if (address < 0 || address > 0xff) throw new IllegalArgumentException("Invalid i2c address " + address)


  def writeByte(data: Int) {
    setSlaveAddress
    file.write(data)
  }


  def writeByte(reg: Int, data: Int) {
    writeBytes(Seq(reg, data))
  }


  def writeBytes(data: Seq[Int]) {
    setSlaveAddress
    file.write(data.map(_.asInstanceOf[Byte]).toArray)
  }


  def readByte: Byte = {
    setSlaveAddress
    file.readByte
  }


  def readBytes(length: Int): Seq[Byte] = {
    setSlaveAddress

    val buffer: Array[Byte] = new Array(length)
    val result = file.read(buffer)
    // XXX: when length is right, there is no copying? Right?!
    buffer.take(math.max(0, result))
  }


  private[this] def file = bus.file


  private[this] def setSlaveAddress {
    val result = file.ioctl(Address.SET_SLAVE_ADDRESS, address)

    val ok = result >= 0

    if (!ok) {
      throw new NoSuchElementException("No device at address " + address + " on " + this)
    }
  }


  def isPresent: Boolean = {
    setSlaveAddress
    val bytes = readBytes(1)
    !bytes.isEmpty
  }
}


object Address {
  
  val SET_SLAVE_ADDRESS = 0x0703
}
