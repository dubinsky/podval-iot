/*
 * Copyright 2012-2013 Podval Group.
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


abstract class Gpio {

  def pin(number: Int): Pin = {
    checkPin(number)

    if (pins(number) == null) {
      pins(number) = createPin(number)
    }

    pins(number)
  }


  def numPins: Int


  protected def createPin(number: Int): Pin


  private[this] val pins: Array[Pin] = new Array[Pin](numPins)


  protected final def checkPin(pin: Int) = require(0 <= pin && pin < numPins, "Invalid pin number %s" format pin)
}
