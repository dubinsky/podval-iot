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


object I2c {

  val blockMax = 32

  val setSlaveAddress   = 0x0703  /* Use this slave address */
  val forceSlaveAddress = 0x0706	/* Change slave address			*/
  //  val getFuncs          = 0x0705  /* Get the adapter functionality */
  //  val readAndWrite      = 0x0707	/* Combined R/W transfer (one stop only)*/
  //  val pec               = 0x0708	/* != 0 for SMBus PEC                   */
  val smbusAccess       = 0x0720  /* SMBus-level access */

  //* smbus_access read or write markers */
  val accessRead : Byte = 1
  val accessWrite: Byte = 0


  /* SMBus transaction types (size parameter) */
  val sizeQuick         : Int = 0
  val sizeByte          : Int = 1
  val sizeByteData      : Int = 2
  val sizeWordData      : Int = 3
  val sizeProcCall      : Int = 4
  val sizeBlockData     : Int = 5
  val sizeBlockBrokenI2c: Int = 6
  val sizeBlockProcCall : Int = 7  /* SMBus 2.0 */
  val sizeBlockDataI2c  : Int = 8

  val EBUSY = -16


  // XXX returns underlying access's return value...
  def writeQuick(file: Int, data: Int) = access(file, data, 0, sizeQuick, null)


  def readByte(file: RandomAccessFile, address: Int): Byte = {
    setSlaveAddress(file, address)
    file.readByte
  }


  def writeByte(file: RandomAccessFile, address: Int, data: Int) {
    setSlaveAddress(file, address)
    file.writeByte(data)
  }


  def writeByte(file: Int, data: Int) {
    // XXX process errors
    access(file, accessWrite, data, sizeByte, null)
  }


  def writeByteData(file: Int, command: Int, data: Int) {
    val accessData = new AccessData
    // XXX set type?
    accessData.byte = data.toByte
    access(file, accessWrite, command, sizeByteData, accessData);
  }


  def readShort(file: RandomAccessFile, address: Int): Short = {
    setSlaveAddress(file, address)
    file.readShort
  }


  def writeShort(file: RandomAccessFile, address: Int, data: Int) {
    setSlaveAddress(file, address)
    file.writeShort(data)
  }


  def writeWordData(file: Int, command: Int, data: Int) {
    val accessData = new AccessData
    // XXX set type?
    accessData.word = data.toShort
    access(file, accessWrite, command, sizeWordData, accessData);
  }


  //  def readByte(file: Int, reg: Int): Byte = {
  //  XXX requires the "access" ioctl
  //  }


  def writeByte(file: RandomAccessFile, address: Int, reg: Int, data: Int) = writeBytes(file, address, Seq(reg, data))


  //  def readShort(file: Int, reg: Int): Byte = {
  //  XXX requires the "access" ioctl
  //  }


  def writeShort(file: RandomAccessFile, address:Int, reg: Int, data: Int) = writeBytes(file, address, Seq(reg, (data >> 8), data))


  def readBytes(file: RandomAccessFile, address: Int, length: Int): Seq[Byte] = {
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


  //  def readBytes(file: Int, reg: Int, length: Int): Seq[Byte] = {
  //  XXX requires the "access" ioctl
  //  }


  def writeBytes(file: RandomAccessFile, address: Int, reg: Int, data: Seq[Int]): Unit = writeBytes(file, address, reg +: data)


  //  private[this] val ioctlData = new IoctlData


  private[this] def access(file: Int, readWrite: Int, command: Int, size: Int, data: AccessData): Int = {
    val args = new IoctlData

    args.readWrite = readWrite.toByte
    args.command = command.toByte
    args.siz = size
    args.data = data

    CLib.library.ioctl(file, smbusAccess, args) // address af "args", obviously!
  }


  private[this] def checkAccess(result: Int) = if (result != 0) throw new IOException("SMB bus access failed")


  def setSlaveAddress(file: RandomAccessFile, address: Int) {
    val result = CLib.library.ioctl(Fd.get(file), setSlaveAddress, address)

    if (result < 0) {
      throw if (result == EBUSY) {
        new IllegalStateException("Address is busy " + this)
      } else {
        new IOException("Failed to set slave address for " + this)
      }
    }
  }


  /*
  static inline __s32 i2c_smbus_read_byte(int file) {
    union i2c_smbus_data data;
    if (i2c_smbus_access(file,I2C_SMBUS_READ,0,I2C_SMBUS_BYTE,&data))
      return -1;
    else
      return 0x0FF & data.byte;
  }


  static inline __s32 i2c_smbus_read_byte_data(int file, __u8 command) {
    union i2c_smbus_data data;
    if (i2c_smbus_access(file,I2C_SMBUS_READ,command, I2C_SMBUS_BYTE_DATA,&data))
      return -1;
    else
      return 0x0FF & data.byte;
  }


  static inline __s32 i2c_smbus_read_word_data(int file, __u8 command) {
    union i2c_smbus_data data;
    if (i2c_smbus_access(file,I2C_SMBUS_READ,command, I2C_SMBUS_WORD_DATA,&data))
      return -1;
    else
      return 0x0FFFF & data.word;
  }


  static inline __s32 i2c_smbus_process_call(int file, __u8 command, __u16 value) {
    union i2c_smbus_data data;
    data.word = value;
    if (i2c_smbus_access(file,I2C_SMBUS_WRITE,command,
                         I2C_SMBUS_PROC_CALL,&data))
      return -1;
    else
      return 0x0FFFF & data.word;
  }


  /* Returns the number of read bytes */
  static inline __s32 i2c_smbus_read_block_data(int file, __u8 command, __u8 *values) {
    union i2c_smbus_data data;
    int i;
    if (i2c_smbus_access(file,I2C_SMBUS_READ,command, I2C_SMBUS_BLOCK_DATA,&data))
      return -1;
    else {
      for (i = 1; i <= data.block[0]; i++)
        values[i-1] = data.block[i];
      return data.block[0];
    }
  }

  static inline __s32 i2c_smbus_write_block_data(int file, __u8 command, __u8 length, const __u8 *values) {
    union i2c_smbus_data data;
    int i;
    if (length > 32)
      length = 32;
    for (i = 1; i <= length; i++)
      data.block[i] = values[i-1];
    data.block[0] = length;
    return i2c_smbus_access(file,I2C_SMBUS_WRITE,command,
                            I2C_SMBUS_BLOCK_DATA, &data);
  }

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
    if (i2c_smbus_access(file,I2C_SMBUS_READ,command,
                         length == 32 ? I2C_SMBUS_I2C_BLOCK_BROKEN :
                          I2C_SMBUS_I2C_BLOCK_DATA,&data))
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
    if (i2c_smbus_access(file,I2C_SMBUS_WRITE,command,
                         I2C_SMBUS_BLOCK_PROC_CALL,&data))
      return -1;
    else {
      for (i = 1; i <= data.block[0]; i++)
        values[i-1] = data.block[i];
      return data.block[0];
    }
  }

     */
}
