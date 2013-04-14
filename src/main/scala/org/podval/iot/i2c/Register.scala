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


final class Register(val address: Address, val register: Int) {

  if (register < 0) throw new IllegalArgumentException("Invalid i2c register " + register)


  override def toString = "register " + register + " of " + address


  def readByteData: Int = address.readByteData(register)
  def writeByteData(data: Int) = address.writeByteData(register, data)
  def readWordData: Int = address.readWordData(register)
  def writeWordData(data: Int) = address.writeWordData(register, data)
  def processCall(data: Int): Int = address.processCall(register, data)
  def readBlockData: Seq[Byte] = address.readBlockData(register)
  def writeBlockData(data: Seq[Int]) = address.writeBlockData(register, data)

  def writeByteSimple(data: Int) = address.writeByteSimple(register, data)
  def writeShort(data: Int) = address.writeShort(register, data)
  def writeBytes(data: Seq[Int]): Unit = address.writeBytes(register, data)
}
