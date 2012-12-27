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

package org.podval.raspberrypi


class I2cDevice(bus: I2cBus, address: Int) {

  I2cBus.checkAddress(address)


  final def write(data: Array[Byte]): Int = {
    val setResult = bus.setSlaveAddress(address)
    if (setResult < 0) setResult else bus.write(data)
  }


  final def read(length: Int): Seq[Byte] = {
    val setResult = bus.setSlaveAddress(address)
    if (setResult < 0) Seq() else bus.read(length)
  }
}
