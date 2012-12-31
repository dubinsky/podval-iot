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

package org.podval.iot.system

import java.io.{RandomAccessFile, IOException}

import com.sun.jna.{Pointer, NativeLong}


class MemoryMappedJna(address: Long, length: Int) extends Memory(address) {

  val PROT_READ  = 1
  val PROT_WRITE = 2

  val MAP_SHARED = 0x01
  val MAO_FIXED  = 0x10

  private[this] val pointer: Pointer = {
    val file = new RandomAccessFile("/dev/mem", "rws")

    val result = CLib.library.mmap(
      new Pointer(0),
      new NativeLong(length),
      PROT_READ|PROT_WRITE,
      MAP_SHARED /*|MAP_FIXED*/,
      Fd.get(file),
      new NativeLong(address))

    if (Pointer.nativeValue(result) < 0) {
      throw new IOException("Failed to map /dev/mem")
    }

//    file.close // XXX ?
    result
  }


  // XXX: close -> unmap?


  def getInt(offset: Int): Int = pointer.getInt(offset)


  def putInt(offset: Int, value: Int): Unit = pointer.setInt(offset, value)
}
