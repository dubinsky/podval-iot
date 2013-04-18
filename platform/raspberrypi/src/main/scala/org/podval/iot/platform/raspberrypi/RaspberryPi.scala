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

package org.podval.iot.platform.raspberrypi

import org.podval.iot.i2c.core.I2c


final class RaspberryPi {

  lazy val revision: Int = {
    // XXX: revision is available from /proc/cpuinfo after keyword "Revision"
    // XXX also: in /proc/cmdline, after bcm2708.boardrev
    2
  }


  lazy val i2cController = new I2c


  def i2c0 = i2cController.bus(0)


  def i2c1 = i2cController.bus(1)


  def i2c = if (revision > 1) i2c1 else i2c0


  lazy val gpio = new Bcm2835Gpio
}
