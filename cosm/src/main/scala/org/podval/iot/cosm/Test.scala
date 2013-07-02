package org.podval.iot.cosm

import java.util.Date


object Test {

  def main(args: Array[String]) {
    val datastream = new Cosm("EXfLw-wMV80LVk6xHpMpmo3vLbkScMiY7PAHc7V7_sM").getFeed(89666).getDatastream(1)
    datastream.addDatapoint(Datapoint(22f))
  }
}
