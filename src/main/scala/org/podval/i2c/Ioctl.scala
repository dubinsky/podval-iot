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

package org.podval.i2c

import java.io.{FileInputStream, FileOutputStream, RandomAccessFile, FileDescriptor}


/**
 * See http://www.artima.com/weblogs/viewpost.jsp?thread=179766
 */
final class Ioctl(fileDescriptor: FileDescriptor) {

  import sun.misc.SharedSecrets


  private[this] val fd: Int = SharedSecrets.getJavaIOFileDescriptorAccess.get(fileDescriptor)


  def ioctl(command: Int, data: Int): Int = Ioctl.library.ioctl(fd, command, data)
}


object Ioctl {

  implicit def toIoctl(file: FileInputStream): Ioctl = new Ioctl(file.getFD)


  implicit def toIoctl(file: FileOutputStream): Ioctl = new Ioctl(file.getFD)


  implicit def toIoctl(file: RandomAccessFile): Ioctl = new Ioctl(file.getFD)


  import com.sun.jna.{Native, Library}


  private trait CLib extends Library {
  
    def ioctl(fd: Int, command: Int, data: Int): Int
  }


  private val library: CLib = Native.loadLibrary("c", classOf[CLib]).asInstanceOf[CLib]
}
