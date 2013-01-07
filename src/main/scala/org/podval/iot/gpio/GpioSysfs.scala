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

package org.podval.iot.gpio

import java.io.{File, FileOutputStream, FileInputStream, DataInputStream}


abstract class GpioSysfs extends Gpio {

  override protected def createPin(number: Int): Pin = new Pin(number) {

    def exported_=(value: Boolean): Unit =
      write(GpioSysfs.basePath + (if (value) "export" else "unexport"), number.toString)


    def exported: Boolean = new File(path("")).exists


    def direction_=(value: Direction): Unit = {
      val what = value match {
        case Input  => "in"
        case Output => "out"
        case Pwm    => "pwm"
      }

      write(directionPath, what)
    }
  
  
    def direction: Direction = {
      read(directionPath) match {
        case "in"  => Input
        case "out" => Output
        case "pwm" => Pwm
      }
    }
  
  
    def pull_=(value: Pull): Unit = {
      // XXX
    }
  
  
    def level_=(value: Boolean): Unit = {
      // XXX
    }
  
  
    def level: Boolean = {
      // XXX
      false
    }


    private[this] def directionPath = path("/direction")


    private[this] def path(file: String) = GpioSysfs.basePath + "/gpio" + number + file


    private[this] def write(path: String, what: String) {
      val os = new FileOutputStream(path)
      os.write(what.getBytes)
      os.close
    }


    private[this] def read(path: String): String = {
      // XXX read the first line
      val is = new DataInputStream(new FileInputStream(path))
      val result = is.readLine
      is.close
      result 
   }
  }
}


object GpioSysfs {

  val basePath = "/sys/class/gpio"
}
