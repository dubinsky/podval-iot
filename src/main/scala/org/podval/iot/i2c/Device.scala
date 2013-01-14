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

import org.podval.iot.system.Ioctl.toIoctl

import java.nio.ByteBuffer
import java.io.IOException

import com.sun.jna.Structure


// XXX rename Address?
final class Device(val bus: Bus, val address: Int) {

  if (address < 0 || address > 0xff) throw new IllegalArgumentException("Invalid i2c address " + address)


  def readByte: Byte = {
    setSlaveAddress
    file.readByte
  }


  def writeByte(data: Int) {
    setSlaveAddress
    file.writeByte(data)
  }


  def readShort: Short = {
    setSlaveAddress
    file.readShort
  }


  def writeShort(data: Int) {
    setSlaveAddress
    file.writeShort(data)
  }


//  def readByte(reg: Int): Byte = {
//  XXX requires the "access" ioctl
//  }


  def writeByte(reg: Int, data: Int) = writeBytes(Seq(reg, data))


//  def readShort(reg: Int): Byte = {
//  XXX requires the "access" ioctl
//  }


  def writeShort(reg: Int, data: Int) = writeBytes(Seq(reg, (data >> 8), data))


  def readBytes(length: Int): Seq[Byte] = {
    if (length < 1 || length > 32) {
      throw new IllegalArgumentException("Length must be between 1 and 32")
    }

    setSlaveAddress

    val buffer: Array[Byte] = new Array(length)
    val result = file.read(buffer)
    // XXX: when length is right, there is no copying? Right?!
    buffer.take(math.max(0, result))
  }


  def writeBytes(data: Seq[Int]): Unit = {
    setSlaveAddress
    file.write(data.map(_.asInstanceOf[Byte]).toArray)
  }


//  def readBytes(reg: Int, length: Int): Seq[Byte] = {
//  XXX requires the "access" ioctl
//  }


  def writeBytes(reg: Int, data: Seq[Int]): Unit = writeBytes(reg +: data)


//#define I2C_SMBUS	0x0720	/* SMBus-level access */
//
///* smbus_access read or write markers */
//#define I2C_SMBUS_READ	1
//#define I2C_SMBUS_WRITE	0
//
///* SMBus transaction types (size parameter in the above functions) 
//   Note: these no longer correspond to the (arbitrary) PIIX4 internal codes! */
//#define I2C_SMBUS_QUICK		    0
//#define I2C_SMBUS_BYTE		    1
//#define I2C_SMBUS_BYTE_DATA	    2 
//#define I2C_SMBUS_WORD_DATA	    3
//#define I2C_SMBUS_PROC_CALL	    4
//#define I2C_SMBUS_BLOCK_DATA	    5
//#define I2C_SMBUS_I2C_BLOCK_BROKEN  6
//#define I2C_SMBUS_BLOCK_PROC_CALL   7		/* SMBus 2.0 */
//#define I2C_SMBUS_I2C_BLOCK_DATA    8

//  private[this] val ioctlData = new IoctlData


//  def access(readWrite: Char, command: Byte, size: Int, )
//static inline __s32 i2c_smbus_access(int file, char read_write, __u8 command, 
//                                     int size, union i2c_smbus_data *data)
//{
//	struct i2c_smbus_ioctl_data args;
//
//	args.read_write = read_write;
//	args.command = command;
//	args.size = size;
//	args.data = data;
//	return ioctl(file,I2C_SMBUS,&args);
//}


  private[this] def file = bus.file


  def setSlaveAddress {
    val result = file.ioctl(Device.SET_SLAVE_ADDRESS, address)

    if (result < 0) {
      if (result == Device.EBUSY) {
        throw new IllegalStateException("Device is busy " + this)
      } else {
        throw new IOException("Failed to set slave address for " + this)
      }
    }
  }


  override def toString = "address " + address + " on " + bus
}


//class IoctlData extends Structure {
//
//  var readWrite: Char = _
//  var command: Byte = _
//  var siz: Int = _
////union i2c_smbus_dtaa {
////	__u8 byte;
////	__u16 word;
////	__u8 block[I2C_SMBUS_BLOCK_MAX + 2]; /* block[0] is used for length */
////	                                            /* and one more for PEC */
////};
////	union i2c_smbus_data *data;
//  val buffer: ByteBuffer = ByteBuffer.allocate(Device.blockMax+2)
//}

//final class IoctlDataByReference extends IoctlData with Structure.ByReference {}


object Device {

  val blockMax = 32


  val SET_SLAVE_ADDRESS = 0x0703


  val EBUSY = -16
}
