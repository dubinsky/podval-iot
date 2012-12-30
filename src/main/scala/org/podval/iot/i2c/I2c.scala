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

import java.io.RandomAccessFile

import scala.collection.mutable


final class I2c {

  private val number2bus = mutable.Map[Int, Bus]()


  def bus(number: Int): Bus = {
    if (number2bus.get(number).isEmpty) {
      number2bus.put(number, new Bus(number))
    }

    number2bus(number)
  }


  final class Bus(val number: Int) {
    
    if (number < 0) {
      throw new IllegalArgumentException("Invalid bus number: " + number)
    }
    
    
    val file: RandomAccessFile = new RandomAccessFile(busDevice, "rw")
    
    
    override def toString: String = "i2c bus " + number + " on " + busDevice
    
    
    def busDevice: String = I2c.busDevicePrefix + number
    
    
    def close = {
      file.close
      number2bus.remove(number)
    }
    
    
    def device(value: Int): Device = new Device(this, value)


// XXX scope Device inside; tighten accessibility of vals
//    final class Device(val address: Int) {
//
//      if (address < 0 || address > 0xff) throw new IllegalArgumentException("Invalid i2c address " + address)
//    }
  }
}


object I2c {
  
  val busDevicePrefix = "/dev/i2c-"
}
