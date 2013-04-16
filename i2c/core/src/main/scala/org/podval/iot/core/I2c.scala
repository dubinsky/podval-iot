/*
 * Copyright 2012-2013 Podval Group.
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

package org.podval.iot.i2c.core


final class I2c {

  import scala.collection.mutable


  private val number2bus = mutable.Map[Int, Bus]()


  def bus(number: Int): Bus = {
    if (!number2bus.contains(number)) {
      number2bus.put(number, new Bus(this, number))
    }

    number2bus(number)
  }


  def close(bus: Bus) {
    number2bus.remove(bus.number)
  }
}

// See the sources of the i2c-tools package: i2c-dev.h, smbusmodule, ....


object I2c {

  import org.podval.iot.system.CLib

  //  val forceSlaveAddress = 0x0706	/* Change slave address			*/
  //  val readAndWrite      = 0x0707	/* Combined R/W transfer (one stop only)*/
  //  val pec               = 0x0708	/* != 0 for SMBus PEC                   */

  private val getFuncs = 0x0705  /* Get the adapter functionality */

  def getFuncs(file: Int): Long = {
    val result = new FunctionsBits
    CLib.library.ioctl(file, getFuncs, result)
    result.funcs.longValue()
  }


  def writeQuick(file: Int, address: Int, data: Byte) =
    Transaction(null).readWrite(data).quick.run(file, address, 0)


  def writeByte(file: Int, address: Int, data: Byte) =
    Transaction(null).write.byte.run(file, address, data)


  def readByte(file: Int, address: Int): Byte =
    Transaction(null).read.byte.run(file, address, 0).getByte


  def readByteData(file: Int, address: Int, command: Byte): Byte =
    Transaction(null).read.byteData.run(file, address, command).getByte


  def writeByteData(file: Int, address: Int, command: Byte, data: Byte) =
    Transaction(null).write.byteData.setByte(data).run(file, address, command)


  def readWordData(file: Int, address: Int, command: Byte): Short =
    Transaction(null).read.wordData.run(file, address, command).getWord


  def writeWordData(file: Int, address: Int, command: Byte, data: Short) =
    Transaction(null).write.wordData.setWord(data).run(file, address, command)


  def processCall(file: Int, address: Int, command: Byte, data: Short): Short =
    Transaction(null).write.procCall.setWord(data).run(file, address, command).getWord


  def readBlockData(file: Int, address: Int, command: Byte): Seq[Byte] =
    Transaction(null).read.blockData.run(file, address, command).getBytes


  def writeBlockData(file: Int, address: Int, command: Byte, data: Seq[Byte]) =
    Transaction(null).write.blockData.setBytes(data).run(file, address, command)


  /* Until kernel 2.6.22, the length is hardcoded to 32 bytes. If you
     ask for less than 32 bytes, your code will only work with kernels
     2.6.23 and later. */

  def readBlockDataI2c(file: Int, address: Int, command: Byte, length: Byte): Seq[Byte] =
    Transaction(null).read.blockI2c(length).setLength(length).run(file, address, command).getBytes


  def writeBlockDataI2c(file: Int, address: Int, command: Byte, data: Seq[Byte]) =
    Transaction(null).write.blockBrokenI2c.setBytes(data).run(file, address, command)


  def blockProcessCall(file: Int, address: Int, command: Byte, data: Seq[Byte]) =
    Transaction(null).write.blockProcCall.setBytes(data).run(file, address, command).getBytes


  private val setSlaveAddress   = 0x0703  /* Use this slave address */


  def setSlaveAddress(file: Int, address: Int): Unit = {
    val result = CLib.library.ioctl(file, setSlaveAddress, address)

    if (result != 0) throw new I2cExeption(result)
  }


  // Calls using read/write on the RandomAccessFile - "simple" flavor.

  import java.io.RandomAccessFile


  def readByteSimple(file: Int, randomAccessFile: RandomAccessFile, address: Int): Byte = {
    setSlaveAddress(file, address)
    randomAccessFile.readByte
  }


  def writeByteSimple(file: Int, randomAccessFile: RandomAccessFile, address: Int, data: Byte) {
    setSlaveAddress(file, address)
    randomAccessFile.writeByte(data)
  }


  def readShort(file: Int, randomAccessFile: RandomAccessFile, address: Int): Short = {
    setSlaveAddress(file, address)
    randomAccessFile.readShort
  }


  def writeShort(file: Int, randomAccessFile: RandomAccessFile, address: Int, data: Short) {
    setSlaveAddress(file, address)
    randomAccessFile.writeShort(data)
  }


  def writeByteSimple(file: Int, randomAccessFile: RandomAccessFile, address: Int, register: Byte, data: Byte) =
    writeBytes(file, randomAccessFile, address, Seq(register, data))


  def writeShort(file: Int, randomAccessFile: RandomAccessFile, address: Int, register: Byte, data: Short) =
    writeBytes(file, randomAccessFile, address, Seq(register, (data >> 8).toByte, (data & 0x0f).toByte))


  def writeBytes(file: Int, randomAccessFile: RandomAccessFile, address: Int, register: Byte, data: Seq[Byte]): Unit =
    writeBytes(file, randomAccessFile, address, register +: data)


  def writeBytes(file: Int, randomAccessFile: RandomAccessFile, address: Int, data: Seq[Byte]): Unit = {
    setSlaveAddress(file, address)
    randomAccessFile.write(data.map(_.toByte).toArray)
  }


  def readBytes(file: Int, randomAccessFile: RandomAccessFile, address: Int, length: Int): Seq[Byte] = {
    TransactionBuffer.checkLength(length.toByte)

    setSlaveAddress(file, address)

    val buffer: Array[Byte] = new Array(length)
    val result = randomAccessFile.read(buffer)
    // XXX: when length is right, there is no copying? Right?!
    buffer.take(math.max(0, result))
  }
}
