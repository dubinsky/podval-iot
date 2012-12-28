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

import com.sun.jna.NativeLong

import java.nio.ByteBuffer


final class FileDescriptor(clib: CLib, path: String, isWrite: Boolean) {

  val mode = if (isWrite) 0x02 else 0x00

  val fd = clib.open(path, mode)

  require(fd >= 0, "failed to open " + path)


  def close: Int = clib.close(fd)


  def ioctl(command: Int, data: Int): Int = clib.ioctl(fd, command, data)


  def read(buffer: Array[Byte]): Int =
    clib.read(fd, ByteBuffer.wrap(buffer), new NativeLong(buffer.length)).intValue


  def write(buffer: Array[Byte]): Int =
    clib.write(fd, ByteBuffer.wrap(buffer), new NativeLong(buffer.length)).intValue
}


object FileDescriptor {

  def apply(clib: CLib, path: String): FileDescriptor = apply(clib, path, true)


  def apply(clib: CLib, path: String, isWrite: Boolean): FileDescriptor =
    new FileDescriptor(clib, path, isWrite)
}
