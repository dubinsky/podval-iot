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

import java.io.{FileInputStream, FileOutputStream, RandomAccessFile}
import java.nio.Buffer

import com.sun.jna.Structure

/**
 * See http://www.artima.com/weblogs/viewpost.jsp?thread=179766
 */
final class Ioctl(fd: Int) {

  def this(file: FileInputStream) = this(Fd.get(file))
  def this(file: FileOutputStream) = this(Fd.get(file))
  def this(file: RandomAccessFile) = this(Fd.get(file))


  def ioctl(command: Int, data: Int): Int = CLib.library.ioctl(fd, command, data)


  def ioctl(command: Int, data: Structure): Int = CLib.library.ioctl(fd, command, data)
}


object Ioctl {

  implicit def toIoctl(file: FileInputStream): Ioctl = new Ioctl(file)


  implicit def toIoctl(file: FileOutputStream): Ioctl = new Ioctl(file)


  implicit def toIoctl(file: RandomAccessFile): Ioctl = new Ioctl(file)
}
