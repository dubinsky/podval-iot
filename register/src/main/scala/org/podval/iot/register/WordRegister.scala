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

package org.podval.iot.register


// XXX expose two ByteRegisters
// XXX factor away commonality
trait WordRegister extends Register {

  final override def size: Int = 16


  final override def set(bit: Int, value: Boolean): Unit = {
    checkBit(bit)
    set((if (value) get | mask(bit) else get & ~mask(bit)).toByte)
  }


  final override def set(value: Boolean): Unit = set((if (value) 0xffff else 0x0000).toShort)


  def set(values: Short) =  {
    modified = get != values
    this.values = values
  }


  final override def write(bit: Int, value: Boolean): Unit = {
    set(bit, value)
    flush
  }


  final override def write(value: Boolean): Unit = {
    set(value)
    flush
  }


  final override def write: Unit = write(get)


  def write(values: Short): Unit


  final override def flush: Unit = {
    if (modified) write
    modified = false
  }


  final override def get(bit: Int): Boolean = {
    checkBit(bit)
    isBitSet(get, bit)
  }


  final def get: Short = values


  final override def read(bit: Int): Boolean = {
    checkBit(bit)
    isBitSet(read, bit)
  }


  def read: Short


  final override def load: Unit = set(read)


  private[this] def isBitSet(values: Short, bit: Int): Boolean = (values & mask(bit)) != 0


  private[this] def mask(bit: Int): Short = (1 << bit).toShort


  private[this] var values: Short = 0


  private[this] var modified: Boolean = false



//  def write(data: Short): Unit = address.writeWord(register, data)
//
//
//  def writeLsb = address.writeByte(register, values.toByte)
//
//
//  def writeMsb = address.writeByte((register+1).toByte, (values >> 8).toByte)
//
//
//  def read: Short = address.readWord(register)
//
//
//  def readLsb: Byte = address.readByte(register)
//
//
//  def readMsb: Byte = address.readByte((register+1).toByte)
//
//
//  override def read(bit: Int): Boolean = isBitSet(if (bit < 8) readLsb else readMsb, if (bit < 8) bit else bit-8)
//
//
//  override def set(bit: Int, value: Boolean) = values = (if (value) values | mask(bit) else values & ~mask(bit)).toShort
}
