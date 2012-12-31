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

package org.podval.iot.system

import sun.misc.Unsafe


final class MemoryUnsafe(address: Long) extends Memory(address) {

  private[this] val unsafe: Unsafe = {
    val theUnsafeField = classOf[Unsafe].getDeclaredField("theUnsafe")
    theUnsafeField.setAccessible(true)
    theUnsafeField.get(null).asInstanceOf[Unsafe]
  }


  def getInt(offset: Int): Int = unsafe.getInt(address + offset)


  def putInt(offset: Int, value: Int): Unit = unsafe.putInt(address + offset, value)
}
