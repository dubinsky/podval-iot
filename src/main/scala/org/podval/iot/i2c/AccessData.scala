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

package org.podval.iot.i2c

import com.sun.jna.Union


final class AccessData extends Union {
  var byte: Byte = _
  var word: Short = _
  // array itself - not a pointer
  // ByteBuffer?
  var block: Array[Byte] = _ // new Array[Byte](Address.blockMax + 2) /* block[0] is used for length and one more for PEC */
}
