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


final class ByteRegister(address: Address, register: Byte) extends Register(address, register) {

  override def write: Unit = write(values)


  def write(data: Byte) = address.writeByte(register, data)


  override def writeForBit(bit: Int): Unit = write


  def read: Byte = address.readByte(register)


  override def read(bit: Int): Boolean = isBitSet(address.readByte(register), bit)


  override def set(bit: Int, value: Boolean) = values = (if (value) values | mask(bit) else values & ~mask(bit)).toByte


  def set(value: Boolean) = set((if (value) 0xff else 0x00).toByte)


  def set(values: Byte) = this.values = values


  override def get(bit: Int): Boolean = isBitSet(values, bit)


  def get: Byte = values


  override def load = set(read)


  private[this] def isBitSet(values: Byte, bit: Int): Boolean = (values & mask(bit)) != 0


  private[this] def mask(bit: Int): Byte = {
    require(0 <= bit && bit <= 7, "Invalid bit number %s; must be between 0 and 7" format bit)
    (1 << bit).toByte
  }


  private[this] var values: Byte = 0
}
