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

import com.sun.jna.{Native, Library, NativeLong}

import java.nio.ByteBuffer


trait CLib extends Library {
  
  def open(path: String, mode: Int): Int
  
  
  def close(fd: Int): Int
  
  
  def ioctl(fd: Int, command: Int, data: Int): Int


  def read(fd: Int, buffer: ByteBuffer, length: NativeLong): NativeLong  


  def write(fd: Int, buffer: ByteBuffer, length: NativeLong): NativeLong  
}



object CLib {

  val clib: CLib = Native.loadLibrary("c", classOf[CLib]).asInstanceOf[CLib]
}
