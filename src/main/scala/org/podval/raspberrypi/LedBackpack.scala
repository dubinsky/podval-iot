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

package org.podval.raspberrypi


final class LedBackpack(bus: I2cBus, number: Int) extends I2cDevice(bus, 0x70 << 1 + number << 1) {

  // XXX: check that the number is legal

  val HT16K33_REGISTER_DISPLAY_SETUP: Byte = 0x80.asInstanceOf[Byte]
  val HT16K33_REGISTER_SYSTEM_SETUP : Byte = 0x20.asInstanceOf[Byte]
  val HT16K33_REGISTER_DIMMING      : Byte = 0xE0.asInstanceOf[Byte]


  def setBrightness(brightness: Int) {
    require(brightness >= 0 && brightness <= 15)
//    write(Seq[Byte]{ HT16K33_REGISTER_DIMMING | brightness, 0x00.asInstanceOf[Byte] })
  }
}

/*

  # Blink rate
  __HT16K33_BLINKRATE_OFF                 = 0x00
  __HT16K33_BLINKRATE_2HZ                 = 0x01
  __HT16K33_BLINKRATE_1HZ                 = 0x02
  __HT16K33_BLINKRATE_HALFHZ              = 0x03

  # Display buffer (8x16-bits)
  __buffer = [0x0000, 0x0000, 0x0000, 0x0000, \
              0x0000, 0x0000, 0x0000, 0x0000 ]

  # Constructor
  def __init__(self, address=0x70, debug=False):
    self.i2c = Adafruit_I2C(address)
    self.address = address
    self.debug = debug

    # Turn the oscillator on
    self.i2c.write8(self.__HT16K33_REGISTER_SYSTEM_SETUP | 0x01, 0x00)

    # Turn blink off
    self.setBlinkRate(self.__HT16K33_BLINKRATE_OFF)

    # Set maximum brightness
    self.setBrightness(15)

    # Clear the screen
    self.clear()

  def setBrightness(self, brightness):
    "Sets the brightness level from 0..15"
    if (brightness > 15):
      brightness = 15
    self.i2c.write8(self.__HT16K33_REGISTER_DIMMING | brightness, 0x00)

  def setBlinkRate(self, blinkRate):
    "Sets the blink rate"
    if (blinkRate > self.__HT16K33_BLINKRATE_HALFHZ):
       blinkRate = self.__HT16K33_BLINKRATE_OFF
    self.i2c.write8(self.__HT16K33_REGISTER_DISPLAY_SETUP | 0x01 | (blinkRate << 1), 0x00)

  def setBufferRow(self, row, value, update=True):
    "Updates a single 16-bit entry in the 8*16-bit buffer"
    if (row > 7):
      return                    # Prevent buffer overflow
    self.__buffer[row] = value  # value # & 0xFFFF
    if (update):
      self.writeDisplay()       # Update the display

  def getBuffer(self):
    "Returns a copy of the raw buffer contents"
    bufferCopy = copy(self.__buffer)
    return bufferCopy
 
  def writeDisplay(self):
    "Updates the display memory"
    bytes = []
    for item in self.__buffer:
      bytes.append(item & 0xFF)
      bytes.append((item >> 8) & 0xFF)
    self.i2c.writeList(0x00, bytes)

  def clear(self, update=True):
    "Clears the display memory"
    self.__buffer = [ 0, 0, 0, 0, 0, 0, 0, 0 ]
    if (update):
      self.writeDisplay()
*/
