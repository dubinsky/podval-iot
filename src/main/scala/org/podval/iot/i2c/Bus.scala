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

import java.io.RandomAccessFile


/*
 * XXX I can not figure out how to:
 * - nest Bus inside I2c so that it can do the close, but not anybody else
 * - nest Device within the Bus so that it can access file, but not anybody else
 * - define Bus and - more importantly, Device - in a separate file
 */
final class Bus(val i2c: I2c, val number: Int) {
  
  if (number < 0) {
    throw new IllegalArgumentException("Invalid bus number: " + number)
  }
  
  
  val file: RandomAccessFile = new RandomAccessFile(busDevice, "rw")
  
  
  override def toString: String = "i2c bus " + number + " on " + busDevice
  
  
  def busDevice: String = Bus.devicePrefix + number
  
  
  def close = {
    i2c.close(this)
    file.close
  }
  
  
  def device(value: Int): Device = new Device(this, value)
}


object Bus {
  
  val devicePrefix = "/dev/i2c-"
}
