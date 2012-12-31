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

package org.podval.iot.raspberrypi

import org.podval.iot.gpio.{Gpio, BitField, Direction, Input, Output, Pull, PullOff, PullDown, PullUp}


/**
 * Inspired by Ben Croston's RPi.GPIO (http://pypi.python.org/pypi/RPi.GPIO).
 */
final class Bcm2835Gpio extends Gpio {

  private[this] val bcm2708PeripheralsBase: Long = 0x20000000
  private[this] val bcm2708GpioOffset     : Long =   0x200000
  
  override def memoryAddress: Long = bcm2708PeripheralsBase + bcm2708GpioOffset
  override def memoryLength = 0xB1


  val fselField      = createField(0x00, 3)
  val setField       = createField(0x1c, 1)
  val clearField     = createField(0x28, 1)
  val levelField     = createField(0x34, 1)
  val pullField      = createField(0x94, 2)
  val pullClockField = createField(0x98, 1)

  // XXX do "event" methods
//  val EVENT_DETECT_OFFSET = 16  // 0x0040 / 4
//  val RISING_ED_OFFSET    = 19  // 0x004c / 4
//  val FALLING_ED_OFFSET   = 22  // 0x0058 / 4
//  val HIGH_DETECT_OFFSET  = 25  // 0x0064 / 4
//  val LOW_DETECT_OFFSET   = 28  // 0x0070 / 4


  def fromPull(value: Pull): Int = value match {
    case PullOff  => 0x0
    case PullDown => 0x1
    case PullUp   => 0x2
  }
  

  // XXX how can I define this class in a separate file?
  override def pin(number: Int): Pin = new Pin(number) {

    override def numPins = 54


    override def direction_=(value: Direction) = fselField.set(number, if (value == Output) 1 else 0)


    override def direction: Direction = if (fselField.get(number) != 0) Output else Input


    // XXX: do the "alt function" methods

    override def pull_=(value: Pull) = {
      pullField.set(0, fromPull(value))
      shortWait
      
      pullClockField.write(number, 0x1)
      shortWait
      
      pullField.set(0, 0x0)
      pullClockField.write(number, 0x0)
    }


    def level_=(value: Boolean) = (if (value) setField else clearField).write(number, 0x1)


    def level: Boolean = levelField.get(number) != 0


    private[this] def shortWait {
      //XXX 150 cycles = ? ms
      Thread.sleep(150)
    }


    // XXX do interrupts
  }
}
