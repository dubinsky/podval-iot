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

package org.podval.iot.i2c


object Functions {

  final case class Descriptor(bit: Long, name: String) {

    def isSupported(bits: Long): Boolean = (bits & bit) != 0
  }


  val writeQuick = Descriptor(0x00010000, "SMBus Quick Command")
  var readByte = Descriptor(0x00020000, "SMBus Receive Byte")


  val all: Seq[Descriptor] = Seq(
    Descriptor(0x00000001, "I2C"),
    writeQuick,
    Descriptor(0x00040000, "SMBus Send Byte"),
    readByte,
    Descriptor(0x00100000, "SMBus Write Byte"),
    Descriptor(0x00080000, "SMBus Read Byte"),
    Descriptor(0x00400000, "SMBus Write Word"),
    Descriptor(0x00200000, "SMBus Read Word"),
    Descriptor(0x00800000, "SMBus Process Call"), /* SMBus 2.0 */
    Descriptor(0x02000000, "SMBus Block Write"),
    Descriptor(0x01000000, "SMBus Block Read"),
    Descriptor(0x00008000, "SMBus Block Process Call"),
    Descriptor(0x00000008, "SMBus PEC"),
    Descriptor(0x08000000, "I2C Block Write"), /* w/ 1-byte reg. addr. */
    Descriptor(0x04000000, "I2C Block Read") /* I2C-like block xfer  */
  )
}
