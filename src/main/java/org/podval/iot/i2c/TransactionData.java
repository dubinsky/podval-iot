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

package org.podval.iot.i2c;

import com.sun.jna.Structure;


// JNA Structures do not work in Scala: JNA expects public fields, which Scala does not generate (it uses methods instead).
// One way around it is to use modified JNA; see https://code.google.com/p/scala-native-access/.
// I want to use stock JNA, so the only way seems to be to write the Structures in Java :(
public final class TransactionData extends Structure {

    public static int BLOCK_MAX = 32;


    public byte readWrite; // char? (=signed byte?)


    public byte command;


    public int size;

    // XXX Having a block here always is simpler for me than dealing with the Union...
    //  but, for a couple of calls, null is expected instead of the block's address; will it work with non-null?
    public byte[] block = new byte[BLOCK_MAX + 2]; /* block[0] is used for length and one more for PEC */
}
