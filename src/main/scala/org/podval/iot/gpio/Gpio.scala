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

/**
 * Memory-mapped interface to the GPIO functionality.
 */
package org.podval.iot.gpio

import org.podval.iot.system.Memory


abstract class Gpio {

  protected def memoryAddress: Long


  protected def memoryLength: Int


  // XXX how to make this accessible only to Pin subclasses?
  val memory = Memory.map(memoryAddress, memoryLength)


  abstract class Pin(number: Int) {
    if (number < 0 || number >= numPins) {
      throw new IllegalArgumentException("Invalid pin number " + number)
    }

    
    def numPins: Int


    def direction_=(value: Direction): Unit


    def direction: Direction


    def pull_=(value: Pull): Unit


    def level_=(value: Boolean): Unit


    def level: Boolean
  }


  def pin(number: Int): Pin
}
