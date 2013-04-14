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

package org.podval.iot.i2c

import org.podval.iot.system.{CLib, Fd}

import java.io.{RandomAccessFile, IOException}

import scala.collection.mutable


final class I2c {

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

  // val forceSlaveAddress = 0x0706	/* Change slave address			*/
  //  val getFuncs          = 0x0705  /* Get the adapter functionality */
  //  val readAndWrite      = 0x0707	/* Combined R/W transfer (one stop only)*/
  //  val pec               = 0x0708	/* != 0 for SMBus PEC                   */


  /* SMBus transaction types (size parameter) */
  val quick         : Int = 0
  val byte          : Int = 1
  val byteData      : Int = 2
  val wordData      : Int = 3
  val procCall      : Int = 4
  val blockData     : Int = 5
  val blockBrokenI2c: Int = 6
  val blockProcCall : Int = 7
  val blockDataI2c  : Int = 8


  // XXX returns underlying access's return value...
  def writeQuick(file: Int, address: Int, data: Int) = {
    setSlaveAddress(file, address)

    val transaction = Transaction.get(data, 0, quick)
    transaction.run(file) // XXX accessData must be null -?
  }


  def readByte(file: Int, address: Int): Int = {
    setSlaveAddress(file, address)

    val transaction = Transaction.read(0, byte)
    checkRead(transaction.run(file))
    transaction.byte
  }


  def writeByte(file: Int, address: Int, data: Int) {
    setSlaveAddress(file, address)

    val transaction = Transaction.write(data, byte)
    // XXX process errors
    transaction.run(file) // XXX accessData must be null -?
  }


  // XXX "command" below is the same as "register"...

  def readByteData(file: Int, address: Int, command: Int): Int = {
    setSlaveAddress(file, address)

    val transaction = Transaction.read(command, byteData)
    checkRead(transaction.run(file))
    transaction.byte
  }


  def writeByteData(file: Int, address: Int, command: Int, data: Int) {
    setSlaveAddress(file, address)

    val transaction = Transaction.write(command, byteData)
    transaction.byte = data.toByte
    // XXX process errors
    transaction.run(file)
  }


  def readWordData(file: Int, address: Int, command: Int): Int = {
    setSlaveAddress(file, address)

    val transaction = Transaction.read(command, wordData)
    checkRead(transaction.run(file))
    transaction.word
  }


  def writeWordData(file: Int, address: Int, command: Int, data: Int) {
    setSlaveAddress(file, address)

    val transaction = Transaction.write(command, wordData)
    transaction.word = data.toShort
    // XXX process errors
    transaction.run(file)
  }


  def processCall(file: Int, address: Int, command: Int, data: Int): Int = {
    setSlaveAddress(file, address)

    val transaction = Transaction.write(command, procCall)
    transaction.word = data.toShort
    checkRead(transaction.run(file))
    transaction.word
  }


  def readBlockData(file: Int, address: Int, command: Int): Seq[Byte] = {
    setSlaveAddress(file, address)

    val transaction = Transaction.read(command, blockData)
    checkRead(transaction.run(file))
    // XXX data.block[0] is the length
    null
  }


  //  def readBytes(file: Int, reg: Int, length: Int): Seq[Byte] = {
  //  XXX requires the "access" ioctl
  //  }


  def writeBlockData(file: Int, address: Int, command: Int, data: Seq[Int]) {
    setSlaveAddress(file, address)

    val transaction = Transaction.write(command, blockData)
    // XXX do I need low-level thing with an array instead of a Sequence? I hope not...
    // XXX implement:
//    int i;
//    if (length > 32)
//      length = 32;
//    for (i = 1; i <= length; i++)
//    data.block[i] = values[i-1];
//    data.block[0] = length;
    transaction.run(file)
  }


  /*

  /* Returns the number of read bytes */
  /* Until kernel 2.6.22, the length is hardcoded to 32 bytes. If you
     ask for less than 32 bytes, your code will only work with kernels
     2.6.23 and later. */
  static inline __s32 i2c_smbus_read_i2c_block_data(int file, __u8 command, __u8 length, __u8 *values) {
    union i2c_smbus_data data;
    int i;

    if (length > 32)
      length = 32;
    data.block[0] = length;
    if (i2c_smbus_access(file,I2C_SMBUS_READ,command, length == 32 ? I2C_SMBUS_I2C_BLOCK_BROKEN : I2C_SMBUS_I2C_BLOCK_DATA,&data))
      return -1;
    else {
      for (i = 1; i <= data.block[0]; i++)
        values[i-1] = data.block[i];
      return data.block[0];
    }
  }

  static inline __s32 i2c_smbus_write_i2c_block_data(int file, __u8 command, __u8 length, const __u8 *values) {
    union i2c_smbus_data data;
    int i;
    if (length > 32)
      length = 32;
    for (i = 1; i <= length; i++)
      data.block[i] = values[i-1];
    data.block[0] = length;
    return i2c_smbus_access(file,I2C_SMBUS_WRITE,command, I2C_SMBUS_I2C_BLOCK_BROKEN, &data);
  }

  /* Returns the number of read bytes */
  static inline __s32 i2c_smbus_block_process_call(int file, __u8 command, __u8 length, __u8 *values) {
    union i2c_smbus_data data;
    int i;
    if (length > 32)
      length = 32;
    for (i = 1; i <= length; i++)
      data.block[i] = values[i-1];
    data.block[0] = length;
    if (i2c_smbus_access(file,I2C_SMBUS_WRITE,command, I2C_SMBUS_BLOCK_PROC_CALL,&data))
      return -1;
    else {
      for (i = 1; i <= data.block[0]; i++)
        values[i-1] = data.block[i];
      return data.block[0];
    }
  }
  */


  private[this] def checkRead(result: Int) = if (result != 0) throw new IOException("SMB bus access failed")


  private val setSlaveAddress   = 0x0703  /* Use this slave address */
  private val EBUSY = -16


  def setSlaveAddress(file: Int, address: Int): Unit = {
    val result = CLib.library.ioctl(file, setSlaveAddress, address)

    if (result < 0) {
      throw if (result == EBUSY) {
        new IllegalStateException("Address is busy " + this)
      } else {
        new IOException("Failed to set slave address for " + this)
      }
    }
  }


  def readByteSimple(file: RandomAccessFile, address: Int): Byte = {
    setSlaveAddress(file, address)
    file.readByte
  }


  def writeByteSimple(file: RandomAccessFile, address: Int, data: Int) {
    setSlaveAddress(file, address)
    file.writeByte(data)
  }


  def readShort(file: RandomAccessFile, address: Int): Short = {
    setSlaveAddress(file, address)
    file.readShort
  }


  def writeShort(file: RandomAccessFile, address: Int, data: Int) {
    setSlaveAddress(file, address)
    file.writeShort(data)
  }


  def writeByteSimple(file: RandomAccessFile, address: Int, reg: Int, data: Int) = writeBytes(file, address, Seq(reg, data))


  def writeShort(file: RandomAccessFile, address:Int, reg: Int, data: Int) = writeBytes(file, address, Seq(reg, (data >> 8), data))


  def readBytes(file: RandomAccessFile, address: Int, length: Int): Seq[Byte] = {
    // XXX swallow into Transaction? Use blockMax, anyway...
    if (length < 1 || length > 32) {
      throw new IllegalArgumentException("Length must be between 1 and 32")
    }

    setSlaveAddress(file, address)

    val buffer: Array[Byte] = new Array(length)
    val result = file.read(buffer)
    // XXX: when length is right, there is no copying? Right?!
    buffer.take(math.max(0, result))
  }


  def writeBytes(file: RandomAccessFile, address: Int, data: Seq[Int]): Unit = {
    setSlaveAddress(file, address)
    file.write(data.map(_.toByte).toArray)
  }


  def writeBytes(file: RandomAccessFile, address: Int, reg: Int, data: Seq[Int]): Unit = writeBytes(file, address, reg +: data)


  def setSlaveAddress(file: RandomAccessFile, address: Int): Unit = setSlaveAddress(Fd.get(file), address)
}
