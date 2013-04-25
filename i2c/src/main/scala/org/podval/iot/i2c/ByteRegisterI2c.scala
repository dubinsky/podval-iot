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

import org.podval.iot.register.ByteRegister


final class ByteRegisterI2c(val address: Address, val register: Byte) extends ByteRegister {

  require(0 <= register, "Invalid i2c register " + register)


  override def toString = "byte register " + register + " of " + address


  override def write(data: Byte) = address.writeByte(register, data)


  override def read: Byte = address.readByte(register)
}
