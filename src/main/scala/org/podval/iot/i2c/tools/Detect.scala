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

package org.podval.iot.i2c.tools

import org.podval.iot.i2c.{I2c, Bus, Device}


object Detect {

  sealed trait Mode
  case object Quick extends Mode
  case object Read extends Mode
  case object Default extends Mode


  def isPresent(device: Device, mode: Mode): Boolean = {
    // XXX: If setSlaveAddress fails with EBUSY, print "UU "; with anything else - abort!

    try {
      mode match {
//      case Device.Quick => 
//        // This is known to corrupt the Atmel AT24RF08 EEPROM
//        writeQuick(I2C_SMBUS_WRITE)
      case Read =>
        // This is known to lock SMBus on various write-only chips (mainly clock chips)
        readByte
      case Default =>
//        if ((0x30 <= device.address && device.address <= 0x37) || (0x50 <= device.address && device.address <= 0x5F))
        readByte
//        else
//        device.writeQuick(I2C_SMBUS_WRITE)
      }

      true

    } catch {
      case e: Exception => false
    }
  }


  def scan(bus: Bus, mode: Mode = Default, first: Int = 0x03, last: Int = 0x77) {
    println("     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f")

    for (i <- 0 until 128 by 16) {
      val line = for (j <- 0 until 16) yield {
        val device = bus.device(i+j)

        /* Skip unwanted addresses */
        if (device.address < first || device.address > last)
          "  "
        else if (isPresent(device, mode))
          ("%02x" format device.address)
        else
          "--"
      }

      println(("%02X: " format i) + line.mkString(" "))
    }
  }


  def main(args: Array[String]) {
    scan((new I2c).bus(1))
  }
}
