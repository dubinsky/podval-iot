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

package org.podval.iot.register


trait Register {

  def size: Int


  def set(bit: Int, value: Boolean): Unit


  def set(value: Boolean): Unit


  def write(bit: Int, value: Boolean): Unit


  def write(value: Boolean): Unit


  def write: Unit


  def flush: Unit


  def get(bit: Int): Boolean


  def read(bit: Int): Boolean


  def load: Unit


  protected final def checkBit(bit: Int) = require(0 <= bit && bit <= size-1, "Invalid bit number %s; must be between 0 and %s" format (bit, size-1))
}
