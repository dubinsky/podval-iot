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


trait ByteRegister extends Register {

  final override def size: Int = 8


  final override def set(bit: Int, value: Boolean): Unit = {
    checkBit(bit)
    set((if (value) get | mask(bit) else get & ~mask(bit)).toByte)
  }


  final override def set(value: Boolean): Unit = set((if (value) 0xff else 0x00).toByte)


  def set(values: Byte) =  {
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


  def write(values: Byte): Unit


  final override def flush: Unit = {
    if (modified) write
    modified = false
  }


  final override def get(bit: Int): Boolean = {
    checkBit(bit)
    isBitSet(get, bit)
  }


  final def get: Byte = values


  final override def read(bit: Int): Boolean = {
    checkBit(bit)
    isBitSet(read, bit)
  }


  def read: Byte


  final override def load: Unit = set(read)


  private[this] def isBitSet(values: Byte, bit: Int): Boolean = (values & mask(bit)) != 0


  private[this] def mask(bit: Int): Byte = (1 << bit).toByte


  private[this] var values: Byte = 0


  private[this] var modified: Boolean = false
}
