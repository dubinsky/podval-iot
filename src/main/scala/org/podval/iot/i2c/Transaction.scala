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

import org.podval.iot.system.CLib
import org.podval.iot.i2c.I2cExeption


final class Transaction {

  /*private*/ val data = new TransactionData


  def read : Transaction = setReadWrite(1)
  def write: Transaction = setReadWrite(0)
  def readWrite(value: Int): Transaction = setReadWrite(value.toByte)
  private[this] def setReadWrite(value: Byte): Transaction = { data.readWrite = value; this }

  def quick         : Transaction = setSize(0)
  def byte          : Transaction = setSize(1)
  def byteData      : Transaction = setSize(2)
  def wordData      : Transaction = setSize(3)
  def procCall      : Transaction = setSize(4)
  def blockData     : Transaction = setSize(5)
  def blockBrokenI2c: Transaction = setSize(6)
  def blockProcCall : Transaction = setSize(7)
  def blockDataI2c  : Transaction = setSize(8)
  private[this] def setSize(value: Byte): Transaction = { data.size = value; this }


  def command(value: Int): Transaction = { data.command = value.toByte; this }


  def getByte: Byte = {
    data.buffer.setType(classOf[Byte])
    data.buffer.byte_
  }


  def setByte(value: Byte): Transaction = {
    data.buffer.setType(classOf[Byte])
    data.buffer.byte_ = value
    this
  }


  def getWord: Short = {
    data.buffer.setType(classOf[Short])
    data.buffer.word
  }


  def setWord(value: Short): Transaction = {
    data.buffer.setType(classOf[Short])
    data.buffer.word
    this
  }


  def getBytes: Seq[Byte] = {
    data.buffer.setType(classOf[Array[Byte]])
    val length = data.buffer.block(0)
    // XXX handle incorrect length
    data.buffer.block.tail.take(length)
  }


  def setBytes(value: Seq[Byte]): Transaction = {
    data.buffer.setType(classOf[Array[Byte]])
    val length = value.length
    // XXX handle incorrect length!!!
    data.buffer.block(0) = length.toByte
    for (i <- 0 until length)
      data.buffer.block(i + 1) = value(i)
    this
  }


  def run(file: Int, address: Int): Transaction = {
    I2c.setSlaveAddress(file, address)

    val result = CLib.library.ioctl(file, Transaction.smbusAccess, data)

    if (result != 0) throw new I2cExeption(result)

    this
  }
}


object Transaction {

  private val smbusAccess = 0x0720  /* SMBus-level access */


  def apply(transaction: Transaction): Transaction = if (transaction != null) transaction else new Transaction
}
