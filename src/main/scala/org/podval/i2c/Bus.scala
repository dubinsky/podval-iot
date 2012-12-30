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

package org.podval.i2c

import java.io.RandomAccessFile


final class Bus(val number: Int) {

  if (number < 0) {
    throw new IllegalArgumentException("Invalid bus number: " + number)
  }


  val file: RandomAccessFile = new RandomAccessFile(busDevice, "rw")


  override def toString: String = "i2c bus " + number + " on " + busDevice


  def busDevice: String = I2c.busDevicePrefix + number


  def close = {
    file.close
    I2c.close(this)
  }


  def device(value: Int): Device = new Device(this, value)
}
