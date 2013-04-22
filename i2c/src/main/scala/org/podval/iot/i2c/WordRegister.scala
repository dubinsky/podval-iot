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


// XXX Expose 2 ByteRegisters!
final class WordRegister(address: Address, register: Byte) extends Register(address, register) {

  override def write: Unit = write(values)


  def write(data: Short): Unit = address.writeWord(register, data)


  def writeLsb = address.writeByte(register, values.toByte)


  def writeMsb = address.writeByte((register+1).toByte, (values >> 8).toByte)


  override def writeForBit(bit: Int): Unit = if (bit < 8) writeLsb else writeMsb


  def read: Short = address.readWord(register)


  def readLsb: Byte = address.readByte(register)


  def readMsb: Byte = address.readByte((register+1).toByte)


  override def read(bit: Int): Boolean = isBitSet(if (bit < 8) readLsb else readMsb, if (bit < 8) bit else bit-8)


  override def set(bit: Int, value: Boolean) = values = (if (value) values | mask(bit) else values & ~mask(bit)).toShort


  def set(value: Boolean) = set((if (value) 0xffff else 0x0000).toShort)


  def set(values: Short) = this.values = values


  override def get(bit: Int): Boolean = isBitSet(values, bit)


  def get: Short = values


  override def load = set(read)


  private[this] def isBitSet(values: Short, bit: Int): Boolean = (values & mask(bit)) != 0


  private[this] def mask(bit: Int): Short = {
    require(0 <= bit && bit <= 15, "Invalid bit number %s; must be between 0 and 15" format bit)
    (1 << bit).toShort
  }


  private[this] var values: Short = 0
}
