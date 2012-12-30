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


object Ioctl {

  def ioctl(file: FileInputStream, command: Int, data: Int): Int = ioctl(getFd(file.getFD), command, data)


  def ioctl(file: FileOutputStream, command: Int, data: Int): Int = ioctl(getFd(file.getFD), command, data)


  def ioctl(file: RandomAccessFile, command: Int, data: Int): Int = ioctl(getFd(file.getFD), command, data)


  import sun.misc.SharedSecrets


  private def getFd(fd: FileDescriptor) = SharedSecrets.getJavaIOFileDescriptorAccess.get(fd)


  import com.sun.jna.{Native, Library}


  private trait Ioctl extends Library {
  
    def ioctl(fd: Int, command: Int, data: Int): Int
  }


  private val ioctl: Ioctl = Native.loadLibrary("c", classOf[Ioctl]).asInstanceOf[Ioctl]


  private def ioctl(fd: Int, command: Int, data: Int): Int = ioctl.ioctl(fd, command, data)
}
