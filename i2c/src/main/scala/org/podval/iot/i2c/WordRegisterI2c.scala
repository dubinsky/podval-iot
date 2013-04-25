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

package org.podval.iot.i2c

import org.podval.iot.register.WordRegister


final class WordRegisterI2c(address: Address, register: Byte) extends WordRegister {

  require(0 <= register, "Invalid i2c register " + register)


  override def toString = "word register " + register + " of " + address


  override def write(data: Short): Unit = address.writeWord(register, data)


  def writeLsb = address.writeByte(register, get.toByte)


  def writeMsb = address.writeByte((register+1).toByte, (get >> 8).toByte)


  override def read: Short = address.readWord(register)


  def readLsb: Byte = address.readByte(register)


  def readMsb: Byte = address.readByte((register+1).toByte)
}
