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


final class Transaction {

  val data = new TransactionData

  def byte: Byte = data.block(0)


  def byte_=(value: Byte) { data.block(0) = value }


  def word = ((data.block(0) << 8) | data.block(1)) & 0xffff


  def word_=(value: Short) = {
    data.block(0) = (value >> 8).toByte
    data.block(1) = (value & 0xff).toByte
  }


  def run(file: Int): Int = {
    CLib.library.ioctl(file, Transaction.smbusAccess, data) // address af "args", obviously!
  }
}


/*
final class AccessData extends Union {
  var byte: Byte = _
  var word: Short = _
  // array itself - not a pointer
  // ByteBuffer?
  var block: Array[Byte] = _ // new Array[Byte](Transaction.blockMax + 2) /* block[0] is used for length and one more for PEC */
}
*/

object Transaction {

  val blockMax = 32


  //* SMBus read or write markers */
  private val read : Byte = 1
  private val write: Byte = 0

  private val smbusAccess = 0x0720  /* SMBus-level access */


  def get(what: Transaction) = if (what != null) what else new Transaction


  def read(command: Int, size: Int): Transaction = get(read, command, size)


  def write(command: Int, size: Int): Transaction = get(write, command, size)


  def get(readWrite: Int, command: Int, size: Int): Transaction = {
    val result = new Transaction
    result.data.readWrite = readWrite.toByte
    result.data.command = command.toByte
    result.data.size = size
    result
  }
}
