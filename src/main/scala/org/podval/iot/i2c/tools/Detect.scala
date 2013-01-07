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


final class Detect(bus: Bus, mode: Detect.Mode, first: Int, last: Int) {
  
  def scan {
    val statuses =
      Map() ++ (for (address <- first to last) yield (address -> status(bus.device(address))))

    println("     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f")

    for (i <- 0 until 128 by 16) {
      val line = for (j <- 0 until 16) yield {
        val address = i+j
        val status = statuses.get(address)
        val legend =
          if (status.isEmpty) "  " else status.get match {
            case Detect.Error   => "EE"
            case Detect.Busy    => "UU"
            case Detect.Absent  => "--"
            case Detect.Present => "%02x" format address
          }
      }

      println(("%02X: " format i) + line.mkString(" "))
    }
  }


  def status(device: Device): Detect.Status = {
    try {
      device.setSlaveAddress
      
      try {
        mode match {
//      case Device.Quick => 
//        // This is known to corrupt the Atmel AT24RF08 EEPROM
//        writeQuick(I2C_SMBUS_WRITE)
          case Detect.Read =>
            // This is known to lock SMBus on various write-only chips (mainly clock chips)
            readByte
          case Detect.Default =>
//        if ((0x30 <= device.address && device.address <= 0x37) || (0x50 <= device.address && device.address <= 0x5F))
            readByte
//        else
//        device.writeQuick(I2C_SMBUS_WRITE)
        }
        
        Detect.Present
        
      } catch {
        case _ => Detect.Absent
      }
    } catch {
      case e: IllegalStateException => Detect.Busy
      case _ => Detect.Error
    }
  }
}


object Detect {

  sealed trait Mode
  case object Quick extends Mode
  case object Read extends Mode
  case object Default extends Mode


  sealed trait Status
  case object Error extends Status
  case object Busy extends Status
  case object Absent extends Status
  case object Present extends Status


  def main(args: Array[String]) {
    new Detect((new I2c).bus(1), Default, 0x03, 0x77).scan
  }
}
