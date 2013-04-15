/*
 * Copyright 2013 Podval Group.
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


final class Register(val address: Address, val register: Byte) {

  if (register < 0) throw new IllegalArgumentException("Invalid i2c register " + register)


  override def toString = "register " + register + " of " + address


  def readByteData: Byte = address.readByteData(register)
  def writeByteData(data: Byte) = address.writeByteData(register, data)
  def readWordData: Short = address.readWordData(register)
  def writeWordData(data: Short) = address.writeWordData(register, data)
  def processCall(data: Short): Int = address.processCall(register, data)
  def readBlockData: Seq[Byte] = address.readBlockData(register)
  def writeBlockData(data: Seq[Byte]) = address.writeBlockData(register, data)
  def readBlockDataI2c(length: Byte): Seq[Byte] = address.readBlockDataI2c(register, length);
  def writeBlockDataI2c(data: Seq[Byte]) = address.writeBlockDataI2c(register, data)
  def blockProcessCall(data: Seq[Byte]) = address.blockProcessCall(register, data)

//  def writeByteSimple(data: Byte) = address.writeByteSimple(register, data)
//  def writeShort(data: Short) = address.writeShort(register, data)
//  def writeBytes(data: Seq[Byte]): Unit = address.writeBytes(register, data)
}
