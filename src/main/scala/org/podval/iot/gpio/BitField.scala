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

package org.podval.iot.gpio

import java.nio.ByteBuffer


class BitField(buffer: ByteBuffer, base: Int, length: Int) {

  val numInInt = 32 / length
  val mask = 0x7fffffff >> (31-length)

  private[this] def offset(number: Int) = base + number / numInInt
  private[this] def shift(number: Int) = (number % numInInt) * length


  def get(number: Int): Int =
    (buffer.getInt(offset(number)) >> shift(number)) & mask


  def set(number: Int, value: Int) = {
    val o = offset(number)
    val s = shift(number)
    buffer.putInt(o, (buffer.getInt(o) & ~(mask<<s)) | ((value & mask) << s))
  }


  def write(number: Int, value: Int) = {
    val o = offset(number)
    val s = shift(number)
    buffer.putInt(o, (value & mask) << s)
  }
}
