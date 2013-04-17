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

package org.podval.iot.i2c.core;

import com.sun.jna.Union;
import com.sun.jna.Structure;


// XXX If, instead of implementing Structure.ByReference right here, I rely on the static inner classes
// (as JNA documentation recommends!) -  code breaks!
public class TransactionBuffer extends Union implements Structure.ByReference {

    public static class ByValue extends TransactionBuffer implements Structure.ByValue {}
    public static class ByReference extends TransactionBuffer implements Structure.ByReference {}


    public static final int BLOCK_MAX = 32;


    public static void checkLength(byte length) {
        if (length < 1 || length > BLOCK_MAX) {
            throw new IllegalArgumentException("Length must be between 1 and " + TransactionBuffer.BLOCK_MAX);
        }
    }


    public byte byte_;


    public short word;


    public byte[] block = new byte[BLOCK_MAX + 2]; // block[0] is used for length and one more for PEC
}
