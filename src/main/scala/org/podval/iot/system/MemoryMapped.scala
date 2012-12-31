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

import java.io.RandomAccessFile

import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


final class MemoryMapped(address: Long, length: Int) extends Memory(address) {

  private[this] val buffer: MappedByteBuffer = {
    val file = new RandomAccessFile("/dev/mem", "rws")
    val result = file.getChannel.map(FileChannel.MapMode.READ_WRITE, address, length)
    file.close
    result
  }


  def getInt(offset: Int): Int = buffer.getInt(offset)


  def putInt(offset: Int, value: Int): Unit = buffer.putInt(offset, value)
}
