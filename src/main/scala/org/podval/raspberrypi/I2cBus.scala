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

package org.podval.raspberrypi


final class I2cBus(bus: Int, fd: FileDescriptor) {

  def close = fd.close


  override def toString: String = "i2c bus " + bus + " on " + I2cBus.busDevice(bus)


  def write(buffer: Array[Byte]): Int = fd.write(buffer)


  def read(length: Int): Seq[Byte] = {
    // XXX: Is this the way?
    val buffer: Array[Byte] = Array.fill[Byte](length)(0xff.asInstanceOf[Byte])
    val result = fd.read(buffer)
    // XXX: where are the Seq methods when you need them?!
    (for (i <- 0 to Math.min(0, result)) yield buffer(i)).toSeq
  }


  def setSlaveAddress(address: Int): Int = {
    I2cBus.checkAddress(address)
    require(address >= 0 && address <= 0xff, "Invalid address " + address)
    fd.ioctl(I2cBus.SET_SLAVE_ADDRESS, address)
  }
}



object I2cBus {

  val busDevicePrefix = "/dev/i2c-"


  val SET_SLAVE_ADDRESS = 0x0703


  def busDevice(bus: Int): String = busDevicePrefix + bus


  def checkAddress(address: Int) = require(address >= 0 && address <= 0xff, "Invalid address " + address)


  def apply(clib: CLib, bus: Int): I2cBus = new I2cBus(bus, FileDescriptor(clib, busDevice(bus)))
}
