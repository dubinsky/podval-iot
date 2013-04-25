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

package org.podval.iot.gpio

import org.podval.iot.register.Register


final class RegisterGpio(
  override val numPins: Int,
  directionR: Register,
  pullUpR: Register,
  inputR: Register,
  outputR: Register) extends Gpio
{
  require(directionR.size == numPins)
  require(pullUpR.size == numPins)
  require(inputR.size == numPins)
  require(outputR.size == numPins)


  // Default: all pins are input, no pull-ups, output values read from the chip.
  directionR.set(true)
  directionR.write
  pullUpR.set(false)
  pullUpR.write
  outputR.load


  protected override def createPin(number: Int): Pin = new Pin(number) {

    def level: Boolean = {
      checkDirection(Input)
      inputR.read(number)
    }


    def level_=(value: Boolean) {
      checkDirection(Output)
      outputR.write(number, value)
    }


    def direction_=(value: Direction) {
      // XXX deal with PWM...
      directionR.write(number, value == Input)
    }


    def direction: Direction = if (directionR.get(number)) Input else Output


    def pull_=(value: Pull) {
      checkDirection(Input)
      pullUpR.write(number, value == PullUp)
    }


    def checkDirection(value: Direction) =
      require(direction == value, "Pin %s not set to %s" format (number, value))
  }
}
