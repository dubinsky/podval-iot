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

package org.podval.iot.i2c


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

  //  val forceSlaveAddress = 0x0706	/* Change slave address			*/
  //  val getFuncs          = 0x0705  /* Get the adapter functionality */
  //  val readAndWrite      = 0x0707	/* Combined R/W transfer (one stop only)*/
  //  val pec               = 0x0708	/* != 0 for SMBus PEC                   */


  // XXX accessData must be null -?
  def writeQuick(file: Int, address: Int, data: Int) =
    Transaction(null).readWrite(data).quick.command(0).run(file, address)


  // XXX accessData must be null -?
  def writeByte(file: Int, address: Int, data: Int) =
    Transaction(null).write.byte.command(data).run(file, address)


  def readByte(file: Int, address: Int): Int =
    Transaction(null).read.byte.command(0).run(file, address).getByte


  def readByteData(file: Int, address: Int, command: Int): Int =
    Transaction(null).read.byteData.command(command).run(file, address).getByte


  def writeByteData(file: Int, address: Int, command: Int, data: Int) =
    Transaction(null).write.byteData.command(command).setByte(data.toByte).run(file, address)


  def readWordData(file: Int, address: Int, command: Int): Int =
    Transaction(null).read.wordData.command(command).run(file, address).getWord


  def writeWordData(file: Int, address: Int, command: Int, data: Int) =
    Transaction(null).write.wordData.command(command).setWord(data.toShort).run(file, address)


  def processCall(file: Int, address: Int, command: Int, data: Int): Int =
    Transaction(null).write.procCall.command(command).setWord(data.toShort).run(file, address).getWord


  def readBlockData(file: Int, address: Int, command: Int): Seq[Byte] =
    Transaction(null).read.blockData.command(command).run(file, address).getBytes


  // XXX do I need low-level thing with an array instead of a Sequence? I hope not...
  def writeBlockData(file: Int, address: Int, command: Int, data: Seq[Int]) =
    Transaction(null).write.blockData.command(command).setBytes(data map (_.toByte)).run(file, address)


  //  def readBytes(file: Int, reg: Int, length: Int): Seq[Byte] = {
  //  XXX requires the "access" ioctl
  //  }

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


  private val setSlaveAddress   = 0x0703  /* Use this slave address */


  def setSlaveAddress(file: Int, address: Int): Unit = {
    import org.podval.iot.system.CLib

    val result = CLib.library.ioctl(file, setSlaveAddress, address)

    if (result != 0) throw new I2cExeption(result)
  }


  // Calls using read/write on the RandomAccessFile - "simple" flavor.

  import java.io.RandomAccessFile


  def readByteSimple(file: Int, randomAccessFile: RandomAccessFile, address: Int): Byte = {
    setSlaveAddress(file, address)
    randomAccessFile.readByte
  }


  def writeByteSimple(file: Int, randomAccessFile: RandomAccessFile, address: Int, data: Int) {
    setSlaveAddress(file, address)
    randomAccessFile.writeByte(data)
  }


  def readShort(file: Int, randomAccessFile: RandomAccessFile, address: Int): Short = {
    setSlaveAddress(file, address)
    randomAccessFile.readShort
  }


  def writeShort(file: Int, randomAccessFile: RandomAccessFile, address: Int, data: Int) {
    setSlaveAddress(file, address)
    randomAccessFile.writeShort(data)
  }


  def writeByteSimple(file: Int, randomAccessFile: RandomAccessFile, address: Int, register: Int, data: Int) =
    writeBytes(file, randomAccessFile, address, Seq(register, data))


  def writeShort(file: Int, randomAccessFile: RandomAccessFile, address:Int, register: Int, data: Int) =
    writeBytes(file, randomAccessFile, address, Seq(register, (data >> 8), data))


  def writeBytes(file: Int, randomAccessFile: RandomAccessFile, address: Int, register: Int, data: Seq[Int]): Unit =
    writeBytes(file, randomAccessFile, address, register +: data)


  def writeBytes(file: Int, randomAccessFile: RandomAccessFile, address: Int, data: Seq[Int]): Unit = {
    setSlaveAddress(file, address)
    randomAccessFile.write(data.map(_.toByte).toArray)
  }


  def readBytes(file: Int, randomAccessFile: RandomAccessFile, address: Int, length: Int): Seq[Byte] = {
    // XXX swallow into Transaction? Use blockMax, anyway...
    if (length < 1 || length > TransactionBuffer.BLOCK_MAX) {
      throw new IllegalArgumentException("Length must be between 1 and " + TransactionBuffer.BLOCK_MAX)
    }

    setSlaveAddress(file, address)

    val buffer: Array[Byte] = new Array(length)
    val result = randomAccessFile.read(buffer)
    // XXX: when length is right, there is no copying? Right?!
    buffer.take(math.max(0, result))
  }
}
