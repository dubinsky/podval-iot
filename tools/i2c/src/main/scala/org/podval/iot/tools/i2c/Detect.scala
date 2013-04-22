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

package org.podval.iot.tools.i2c


import org.kohsuke.args4j.{Option => option, Argument, CmdLineParser, CmdLineException}
import org.podval.iot.i2c._


final class Detect {

  private[this] val i2c = new I2c


  private val usage =
    """
      |	Usage: i2cdetect [-y] [-a] [-q|-r] I2CBUS [FIRST LAST]
      |	       i2cdetect -F I2CBUS
      |	       i2cdetect -l
      |	  I2CBUS is an integer or an I2C bus name
      |	  If provided, FIRST and LAST limit the probing range.
    """.stripMargin


  @option(name="-l", usage="list buses")
  private var listBusesFlag: Boolean = _


  @option(name="-F", usage="print bus functionality")
  private var printFunctionalityFlag: Boolean = _


  @option(name="-q", usage="use quick write for scanning the bus")
  private var useQuickWriteFlag: Boolean = _


  @option(name="-r", usage="use read byte for scanning the bus")
  private var useReadByteFlag: Boolean = _


  @option(name="-a", usage="scan all addresses")
  private var scanAllAddressesFlag: Boolean = _


  @option(name="-y", usage="disable interactive mode")
  private var disableInteractiveMode: Boolean = _


  @Argument
  private val arguments = new java.util.ArrayList[String]()


  private def main(args: Array[String]) = {
    val parser: CmdLineParser = new CmdLineParser(this)

    parser.setUsageWidth(80)

    try {
      // parse the arguments.
      parser. parseArgument(args: _*)
      run(parser)

    } catch {
      case e: CmdLineException =>
        println(e.getMessage())
        println(usage)
        parser.printUsage(System.err)
    }
  }


  private[this] def run(parser: CmdLineParser) {
    val numModes: Int = List[Boolean](listBusesFlag, printFunctionalityFlag, useQuickWriteFlag, useReadByteFlag).count{ p: Boolean => p }
    if (numModes > 2) throw new CmdLineException(parser, "Conflicting modes!")

    val doScan = !listBusesFlag && !printFunctionalityFlag
    if ((scanAllAddressesFlag || disableInteractiveMode) && !doScan)
      throw new CmdLineException(parser, "options '-a' and '-y' are only valid for scanning")

    if (listBusesFlag) {
      if (!arguments.isEmpty) throw new CmdLineException(parser, "superfluous arguments for '-l' ")
      listBusses
    } else

    if (printFunctionalityFlag) {
      if (arguments.isEmpty) throw new CmdLineException(parser, "bus not specified")
      if (arguments.size() > 1) throw new CmdLineException(parser, "superfluous arguments for '-F' ")
      val bus = i2c.bus(getBusNumber(parser, arguments.get(0)))
      printFunctionality(bus)
      bus.close

    } else { // scan
      if (arguments.isEmpty) throw new CmdLineException(parser, "bus not specified")
      if (arguments.size > 3) throw new CmdLineException(parser, "superfluous arguments for scan")
      if ((arguments.size > 1) && scanAllAddressesFlag) throw new CmdLineException(parser, "superfluous arguments for scan with '-a'")
      val first = if (arguments.size > 1) parseAddress(parser, arguments.get(1)) else if (scanAllAddressesFlag) 0x00 else 0x03
      val last  = if (arguments.size > 2) parseAddress(parser, arguments.get(2)) else if (scanAllAddressesFlag) 0x7f else 0x77
      if (first > last) throw new CmdLineException(parser, "first address can't be higher than the last address")

      val bus = i2c.bus(getBusNumber(parser, arguments.get(0)))

      val mode = if (useQuickWriteFlag) Detect.Quick else if (useReadByteFlag) Detect.Read else Detect.Default

      if (disableInteractiveMode || confirm(bus, mode, first, last)) {
        scan(bus, mode, first, last)
      }

      bus.close
    }
  }


  private[this] def confirm(bus: Bus, mode: Detect.Mode, first: Int, last: Int): Boolean = {
    System.err.println("WARNING! This program can confuse your I2C bus, cause data loss and worse!")
    System.err.println("I will probe file " + bus.busDevice +
      (if (mode == Detect.Quick) "using quick write commands" else if (mode == Detect.Read) "using read bytes commands" else "") +".")
    System.err.println("I will probe address range 0x%02x-0x%02x" format(first, last))

    System.err.print("Continue? [Y/n] ")
    System.err.flush

    val answer = System.in.read.toChar

    val result = Seq('\n', 'y', 'Y').contains(answer)
    if (!result) {
      System.err.println("Aborting on user request.")
    }

    result
  }


  private[this] def listBusses {
    // XXX
  }


  private[this] def getBusNumber(parser: CmdLineParser, what: String): Int = {
    // XXX
    1
  }


  private[this] def parseAddress(parser: CmdLineParser, what: String): Int = {
    // XXX
    0x00
  }


  private[this] def printFunctionality(bus: Bus) {
    val bits = bus.getFunctions
    for (function <- Functions.all) {
      println(("%-32s "  format function.name) + (if (function.isSupported(bits)) "yes" else "no"))
    }
  }


  private[this] def scan(bus: Bus, mode: Detect.Mode, first: Int, last: Int) {
    val funcs = bus.getFunctions
    val quickAvailable = Functions.writeQuick.isSupported(funcs)
    val readAvailable = Functions.readByte.isSupported(funcs)

    if ((!quickAvailable && (mode != Detect.Read)) || (!readAvailable && (mode != Detect.Quick)))
      throw new UnsupportedOperationException("functions needed to scan the bus are not available")

    val statuses =
      Map() ++ (for (address <- first to last) yield (address -> status(bus.address(address), mode)))

    println("     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f")

    for (i <- 0 until 128 by 16) {
      val line = for (j <- 0 until 16) yield {
        val address = i+j
        val status = statuses.get(address)
        if (status.isEmpty) "  " else status.get match {
          case Detect.Error   => "EE"
          case Detect.Busy    => "UU"
          case Detect.Absent  => "--"
          case Detect.Present => "%02x" format address
        }
      }

      println(("%02X: " format i) + line.mkString(" "))
    }
  }


  private[this] def status(address: Address, mode: Detect.Mode): Detect.Status = {
    def safeToRead(a: Int) = (0x30 <= a && a <= 0x37) || (0x50 <= a && a <= 0x5F)

    try {
      if ((mode == Detect.Read) || ((mode == Detect.Default) && safeToRead(address.address))) {
        // This is known to lock SMBus on various write-only chips (mainly clock chips)
        address.readByte
      } else {
        // This is known to corrupt the Atmel AT24RF08 EEPROM
        address.writeQuick(0)
      }

      Detect.Present
        
    } catch {
      case e: I2cExeption => if (e.result == Detect.EBUSY) Detect.Busy else Detect.Absent
      case e: Exception => Detect.Error
    }
  }
}


object Detect {

  sealed trait Mode
  case object Quick extends Mode
  case object Read extends Mode
  case object Default extends Mode


  sealed trait Status
  case object Error extends Status
  case object Busy extends Status
  case object Absent extends Status
  case object Present extends Status


  private val EBUSY = -16


  def main(args: Array[String]) = new Detect().main(args)
}
