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

import org.podval.iot.i2c.{I2c, Bus, Address, I2cExeption}


final class Detect(bus: Bus, mode: Detect.Mode, first: Int, last: Int) {
  
  def scan {
    val statuses =
      Map() ++ (for (address <- first to last) yield (address -> status(bus.address(address))))

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
        legend
      }

      println(("%02X: " format i) + line.mkString(" "))
    }
  }


  def status(address: Address): Detect.Status = {
    try {
      mode match {
        case Detect.Quick =>
          // This is known to corrupt the Atmel AT24RF08 EEPROM
          address.writeQuick(0)
        case Detect.Read =>
          // This is known to lock SMBus on various write-only chips (mainly clock chips)
          readByte
        case Detect.Default =>
          if ((0x30 <= address.address && address.address <= 0x37) || (0x50 <= address.address && address.address <= 0x5F))
            address.readByte
          else
            address.writeQuick(0)
      }

      Detect.Present
        
    } catch {
      case e: I2cExeption => if (e.result == Detect.EBUSY) Detect.Busy else Detect.Absent
      case e: Exception => Detect.Error
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


  private val EBUSY = -16


  def main(args: Array[String]) {
    // XXX Add command-line options!
    new Detect((new I2c).bus(1), Default, 0x03, 0x77).scan
  }
}
