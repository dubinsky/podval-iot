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

import com.sun.jna.{Native, Pointer, NativeLong}


/*
 * XXX Native has get/set methods that can be used for random memory access,
 * but they probably will crash just as Unsafe's ones.
 * Also, I am not sure they are there in JNA 3.2.7 (version currently on Raspberry Pi) -
 * even Native.malloc() isn't!
 */
class MemoryMappedJna(address: Long, length: Int, fixed: Boolean) extends Memory(address) {

  val PROT_READ  = 1
  val PROT_WRITE = 2

  val MAP_SHARED = 0x01
  val MAP_FIXED  = 0x10

  val pageSize = 4*1024 // XXX get from Unsafe?

  private[this] val pointer: Pointer = {
    val file = new RandomAccessFile("/dev/mem", "rws")

    val pointer = if (!fixed) new Pointer(0) else {
      var bufferSize: Long = length + (pageSize-1)
      val buffer: Long =
      // XXX malloc() appeared after JNA 3.2.7, which is on Raspberry Pi currently...
      // Native.malloc(bufferSize)
        CLib.library.malloc(new NativeLong(bufferSize)).longValue
          
      if (buffer == 0) {
        throw new IOException("Failed to allocate buffer")
      }
      val pageOffset = buffer % pageSize
      val mapBase: Long = if (pageOffset == 0) buffer else (buffer + pageSize - pageOffset)

      new Pointer(mapBase)
    }


    val result = CLib.library.mmap(
      pointer,
      new NativeLong(length),
      PROT_READ|PROT_WRITE,
      MAP_SHARED | (if (fixed) MAP_FIXED else 0x00),
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
