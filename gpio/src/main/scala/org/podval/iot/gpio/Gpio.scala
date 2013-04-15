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


abstract class Gpio {

  def pin(number: Int): Pin = {
    if (number < 0 || number >= numPins) {
      throw new IllegalArgumentException("Invalid pin number " + number)
    }

    if (pins(number) == null) {
      pins(number) = createPin(number)
    }

    pins(number)
  }


  val numPins: Int


  protected def createPin(number: Int): Pin


  private[this] val pins: Array[Pin] = new Array[Pin](numPins)
}
