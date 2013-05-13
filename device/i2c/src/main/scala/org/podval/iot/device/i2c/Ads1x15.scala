package org.podval.iot.device.i2c

import org.podval.iot.i2c.Bus


final class Ads1x15(bus: Bus, number: Int) {

  require(0 <= number && number <= 7, "Invalid ADS1x15 address " + number)

  private[this] val address = bus.address(0x48 + number)

  // XXX: ic=__IC_ADS1015
  // if ((ic < self.__IC_ADS1015) | (ic > self.__IC_ADS1115)): print "ADS1x15: Invalid IC specfied: %h" % ic

  // Set pga value, so that getLastConversionResult() can use it, any function that accepts a pga value must update this.
  private[this] var pga = 2048


  /*
    Gets a single-ended ADC reading from the specified channel in mV.
    The sample rate for this mode (single-shot) can be used to lower the noise
    (low sps) or to lower the power consumption (high sps) by duty cycling,
    see datasheet page 14 for more info.
    The pga must be given in mV, see page 13 for the supported values.
   */
  def readADCSingleEnded(channel: Int = 0, pga: Int = 2048, sps: Int = 250) = {
    require(0 <= channel && channel <= 3, "Channel must be between 0 and 3")

  // Disable comparator, Non-latching, Alert/Rdy active low traditional comparator, single-shot mode
  var config: Int =
    Ads1x15.__ADS1015_REG_CONFIG_CQUE_NONE    |
    Ads1x15.__ADS1015_REG_CONFIG_CLAT_NONLAT  |
    Ads1x15.__ADS1015_REG_CONFIG_CPOL_ACTVLOW |
    Ads1x15.__ADS1015_REG_CONFIG_CMODE_TRAD   |
    Ads1x15.__ADS1015_REG_CONFIG_MODE_SINGLE

    // Set sample per seconds, defaults to 250sps
    // If sps is in the dictionary (defined in init) it returns the value of the constant
    // othewise it returns the value for 250sps. This saves a lot of if/elif/else code!
    //  if (self.ic == self.__IC_ADS1015):
    //  config |= self.spsADS1015.setdefault(sps, self.__ADS1015_REG_CONFIG_DR_1600SPS)
    //  else:
    //  if ( (sps not in self.spsADS1115) & self.debug):
    //  print "ADS1x15: Invalid pga specified: %d, using 6144mV" % sps
    //    config |= self.spsADS1115.setdefault(sps, self.__ADS1115_REG_CONFIG_DR_250SPS)
    config = config | Ads1x15.__ADS1015_REG_CONFIG_DR_250SPS

    // Set PGA/voltage range, defaults to +-6.144V
    //  if ( (pga not in self.pgaADS1x15) & self.debug):
    //  print "ADS1x15: Invalid pga specified: %d, using 6144mV" % sps
    //    config |= self.pgaADS1x15.setdefault(pga, self.__ADS1015_REG_CONFIG_PGA_6_144V)
    //  self.pga = pga
    config = config | Ads1x15.__ADS1015_REG_CONFIG_PGA_2_048V

    // Set the channel to be converted
    config = config | (
      if (channel == 3) Ads1x15.__ADS1015_REG_CONFIG_MUX_SINGLE_3 else
      if (channel == 2) Ads1x15.__ADS1015_REG_CONFIG_MUX_SINGLE_2 else
      if (channel == 1) Ads1x15.__ADS1015_REG_CONFIG_MUX_SINGLE_1 else
      /*channel == 0*/ Ads1x15.__ADS1015_REG_CONFIG_MUX_SINGLE_0)

    // Set 'start single-conversion' bit
    config = config | Ads1x15.__ADS1015_REG_CONFIG_OS_SINGLE

    // Write config register to the ADC
    //    bytes = [(config >> 8) & 0xFF, config & 0xFF]
    //  self.i2c.writeList(self.__ADS1015_REG_POINTER_CONFIG, bytes)
    address.writeBytes(Ads1x15.__ADS1015_REG_POINTER_CONFIG.toByte, Seq[Byte](((config >> 8) & 0xff).toByte, (config & 0xff).toByte))
//    address.writeWord(Ads1x15.__ADS1015_REG_POINTER_CONFIG.toByte, config.toShort)

    // Wait for the ADC conversion to complete
    // The minimum delay depends on the sps: delay >= 1/sps
    // We add 0.1ms to be sure
    // XXX
    //val delay delay = 1.0/sps+0.0001
    //  time.sleep(delay)
    Thread.sleep(5)

    // Read the conversion results
    //    result = self.i2c.readList(self.__ADS1015_REG_POINTER_CONVERT, 2)
    val result = address.readBytes(Ads1x15.__ADS1015_REG_POINTER_CONVERT.toByte, 2)

    //  if (self.ic == self.__IC_ADS1015):
    //  # Shift right 4 bits for the 12-bit ADS1015 and convert to mV
    //  return ( ((result[0] << 8) | (result[1] & 0xFF)) >> 4 )*pga/2048.0
    (((result(0) << 8) | (result(1) & 0xff)) >> 4)*pga/2048
    //  else:
    //  # Return a mV value for the ADS1115
    //  # (Take signed values into account as well)
    //  val = (result[0] << 8) | (result[1])
    //  if val > 0x7FFF:
    //  return (val - 0xFFFF)*pga/32768.0
    //  else:
    //  return ( (result[0] << 8) | (result[1]) )*pga/32768.0
  }

/*
#!/usr/bin/python

import time
import smbus
from Adafruit_I2C import Adafruit_I2C

# ===========================================================================
# ADS1x15 Class
#
# Originally written by K. Townsend, Adafruit (https://github.com/adafruit/Adafruit-Raspberry-Pi-Python-Code/tree/master/Adafruit_ADS1x15)
# Updates and new functions implementation by Pedro Villanueva, 03/2013.
# The only error in the original code was in line 57:
#              __ADS1015_REG_CONFIG_DR_920SPS    = 0x0050
# should be
#              __ADS1015_REG_CONFIG_DR_920SPS    = 0x0060
#
# NOT IMPLEMENTED: Conversion ready pin, page 15 datasheet.
# ===========================================================================

class ADS1x15:
  i2c = None

  # IC Identifiers
  __IC_ADS1015                      = 0x00
  __IC_ADS1115                      = 0x01

  # Dictionaries with the sampling speed values
  # These simplify and clean the code (avoid the abuse of if/elif/else clauses)
  spsADS1115 = {
    8:__ADS1115_REG_CONFIG_DR_8SPS,
    16:__ADS1115_REG_CONFIG_DR_16SPS,
    32:__ADS1115_REG_CONFIG_DR_32SPS,
    64:__ADS1115_REG_CONFIG_DR_64SPS,
    128:__ADS1115_REG_CONFIG_DR_128SPS,
    250:__ADS1115_REG_CONFIG_DR_250SPS,
    475:__ADS1115_REG_CONFIG_DR_475SPS,
    860:__ADS1115_REG_CONFIG_DR_860SPS
  }
  spsADS1015 = {
    128:__ADS1015_REG_CONFIG_DR_128SPS,
    250:__ADS1015_REG_CONFIG_DR_250SPS,
    490:__ADS1015_REG_CONFIG_DR_490SPS,
    920:__ADS1015_REG_CONFIG_DR_920SPS,
    1600:__ADS1015_REG_CONFIG_DR_1600SPS,
    2400:__ADS1015_REG_CONFIG_DR_2400SPS,
    3300:__ADS1015_REG_CONFIG_DR_3300SPS
  }
  # Dictionariy with the programable gains
  pgaADS1x15 = {
    6144:__ADS1015_REG_CONFIG_PGA_6_144V,
    4096:__ADS1015_REG_CONFIG_PGA_4_096V,
    2048:__ADS1015_REG_CONFIG_PGA_2_048V,
    1024:__ADS1015_REG_CONFIG_PGA_1_024V,
    512:__ADS1015_REG_CONFIG_PGA_0_512V,
    256:__ADS1015_REG_CONFIG_PGA_0_256V
  }




  def readADCDifferential(self, chP=0, chN=1, pga=6144, sps=250):
    "Gets a differential ADC reading from channels chP and chN in mV. \
    The sample rate for this mode (single-shot) can be used to lower the noise \
    (low sps) or to lower the power consumption (high sps) by duty cycling, \
    see data sheet page 14 for more info. \
    The pga must be given in mV, see page 13 for the supported values."

    # Disable comparator, Non-latching, Alert/Rdy active low
    # traditional comparator, single-shot mode
    config = self.__ADS1015_REG_CONFIG_CQUE_NONE    | \
             self.__ADS1015_REG_CONFIG_CLAT_NONLAT  | \
             self.__ADS1015_REG_CONFIG_CPOL_ACTVLOW | \
             self.__ADS1015_REG_CONFIG_CMODE_TRAD   | \
             self.__ADS1015_REG_CONFIG_MODE_SINGLE

    # Set channels
    if ( (chP == 0) & (chN == 1) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_0_1
    elif ( (chP == 0) & (chN == 3) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_0_3
    elif ( (chP == 2) & (chN == 3) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_2_3
    elif ( (chP == 1) & (chN == 3) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_1_3
    else:
      if (self.debug):
	print "ADS1x15: Invalid channels specified: %d, %d" % (chP, chN)
	return -1

    # Set sample per seconds, defaults to 250sps
    # If sps is in the dictionary (defined in init()) it returns the value of the constant
    # othewise it returns the value for 250sps. This saves a lot of if/elif/else code!
    if (self.ic == self.__IC_ADS1015):
      config |= self.spsADS1015.setdefault(sps, self.__ADS1015_REG_CONFIG_DR_1600SPS)
    else:
      if ( (sps not in self.spsADS1115) & self.debug):
	print "ADS1x15: Invalid pga specified: %d, using 6144mV" % sps
      config |= self.spsADS1115.setdefault(sps, self.__ADS1115_REG_CONFIG_DR_250SPS)

    # Set PGA/voltage range, defaults to +-6.144V
    if ( (pga not in self.pgaADS1x15) & self.debug):
      print "ADS1x15: Invalid pga specified: %d, using 6144mV" % sps
    config |= self.pgaADS1x15.setdefault(pga, self.__ADS1015_REG_CONFIG_PGA_6_144V)
    self.pga = pga

    # Set 'start single-conversion' bit
    config |= self.__ADS1015_REG_CONFIG_OS_SINGLE

    # Write config register to the ADC
    bytes = [(config >> 8) & 0xFF, config & 0xFF]
    self.i2c.writeList(self.__ADS1015_REG_POINTER_CONFIG, bytes)

    # Wait for the ADC conversion to complete
    # The minimum delay depends on the sps: delay >= 1/sps
    # We add 0.1ms to be sure
    delay = 1.0/sps+0.0001
    time.sleep(delay)

    # Read the conversion results
    result = self.i2c.readList(self.__ADS1015_REG_POINTER_CONVERT, 2)
    if (self.ic == self.__IC_ADS1015):
    	# Shift right 4 bits for the 12-bit ADS1015 and convert to mV
    	return ( ((result[0] << 8) | (result[1] & 0xFF)) >> 4 )*pga/2048.0
    else:
	# Return a mV value for the ADS1115
	# (Take signed values into account as well)
	val = (result[0] << 8) | (result[1])
	if val > 0x7FFF:
	  return (val - 0xFFFF)*pga/32768.0
	else:
	  return ( (result[0] << 8) | (result[1]) )*pga/32768.0


  def readADCDifferential01(self, pga=6144, sps=250):
    "Gets a differential ADC reading from channels 0 and 1 in mV\
    The sample rate for this mode (single-shot) can be used to lower the noise \
    (low sps) or to lower the power consumption (high sps) by duty cycling, \
    see data sheet page 14 for more info. \
    The pga must be given in mV, see page 13 for the supported values."
    return self.readADCDifferential(0, 1, pga, sps)


  def readADCDifferential03(self, pga=6144, sps=250):
    "Gets a differential ADC reading from channels 0 and 3 in mV \
    The sample rate for this mode (single-shot) can be used to lower the noise \
    (low sps) or to lower the power consumption (high sps) by duty cycling, \
    see data sheet page 14 for more info. \
    The pga must be given in mV, see page 13 for the supported values."
    return self.readADCDifferential(0, 3, pga, sps)


  def readADCDifferential13(self, pga=6144, sps=250):
    "Gets a differential ADC reading from channels 1 and 3 in mV \
    The sample rate for this mode (single-shot) can be used to lower the noise \
    (low sps) or to lower the power consumption (high sps) by duty cycling, \
    see data sheet page 14 for more info. \
    The pga must be given in mV, see page 13 for the supported values."
    return self.__readADCDifferential(1, 3, pga, sps)


  def readADCDifferential23(self, pga=6144, sps=250):
    "Gets a differential ADC reading from channels 2 and 3 in mV \
    The sample rate for this mode (single-shot) can be used to lower the noise \
    (low sps) or to lower the power consumption (high sps) by duty cycling, \
    see data sheet page 14 for more info. \
    The pga must be given in mV, see page 13 for the supported values."
    return self.readADCDifferential(2, 3, pga, sps)


  def startContinuousConversion(self, channel=0, pga=6144, sps=250):
    "Starts the continuous conversion mode and returns the first ADC reading \
    in mV from the specified channel. \
    The sps controls the sample rate. \
    The pga must be given in mV, see datasheet page 13 for the supported values. \
    Use getLastConversionResults() to read the next values and \
    stopContinuousConversion() to stop converting."

    # Default to channel 0 with invalid channel, or return -1?
    if (channel > 3):
      if (self.debug):
	print "ADS1x15: Invalid channel specified: %d" % channel
      return -1

    # Disable comparator, Non-latching, Alert/Rdy active low
    # traditional comparator, continuous mode
    # The last flag is the only change we need, page 11 datasheet
    config = self.__ADS1015_REG_CONFIG_CQUE_NONE    | \
             self.__ADS1015_REG_CONFIG_CLAT_NONLAT  | \
             self.__ADS1015_REG_CONFIG_CPOL_ACTVLOW | \
             self.__ADS1015_REG_CONFIG_CMODE_TRAD   | \
             self.__ADS1015_REG_CONFIG_MODE_CONTIN

    # Set sample per seconds, defaults to 250sps
    # If sps is in the dictionary (defined in init()) it returns the value of the constant
    # othewise it returns the value for 250sps. This saves a lot of if/elif/else code!
    if (self.ic == self.__IC_ADS1015):
      config |= self.spsADS1015.setdefault(sps, self.__ADS1015_REG_CONFIG_DR_1600SPS)
    else:
      if ( (sps not in self.spsADS1115) & self.debug):
	print "ADS1x15: Invalid pga specified: %d, using 6144mV" % sps
      config |= self.spsADS1115.setdefault(sps, self.__ADS1115_REG_CONFIG_DR_250SPS)

    # Set PGA/voltage range, defaults to +-6.144V
    if ( (pga not in self.pgaADS1x15) & self.debug):
      print "ADS1x15: Invalid pga specified: %d, using 6144mV" % sps
    config |= self.pgaADS1x15.setdefault(pga, self.__ADS1015_REG_CONFIG_PGA_6_144V)
    self.pga = pga

    # Set the channel to be converted
    if channel == 3:
      config |= self.__ADS1015_REG_CONFIG_MUX_SINGLE_3
    elif channel == 2:
      config |= self.__ADS1015_REG_CONFIG_MUX_SINGLE_2
    elif channel == 1:
      config |= self.__ADS1015_REG_CONFIG_MUX_SINGLE_1
    else:
      config |= self.__ADS1015_REG_CONFIG_MUX_SINGLE_0

    # Set 'start single-conversion' bit to begin conversions
    # No need to change this for continuous mode!
    config |= self.__ADS1015_REG_CONFIG_OS_SINGLE

    # Write config register to the ADC
    # Once we write the ADC will convert continously
    # we can read the next values using getLastConversionResult
    bytes = [(config >> 8) & 0xFF, config & 0xFF]
    self.i2c.writeList(self.__ADS1015_REG_POINTER_CONFIG, bytes)

    # Wait for the ADC conversion to complete
    # The minimum delay depends on the sps: delay >= 1/sps
    # We add 0.5ms to be sure
    delay = 1.0/sps+0.0005
    time.sleep(delay)

    # Read the conversion results
    result = self.i2c.readList(self.__ADS1015_REG_POINTER_CONVERT, 2)
    if (self.ic == self.__IC_ADS1015):
    	# Shift right 4 bits for the 12-bit ADS1015 and convert to mV
    	return ( ((result[0] << 8) | (result[1] & 0xFF)) >> 4 )*pga/2048.0
    else:
	# Return a mV value for the ADS1115
	# (Take signed values into account as well)
	val = (result[0] << 8) | (result[1])
	if val > 0x7FFF:
	  return (val - 0xFFFF)*pga/32768.0
	else:
	  return ( (result[0] << 8) | (result[1]) )*pga/32768.0

  def startContinuousDifferentialConversion(self, chP=0, chN=1, pga=6144, sps=250):
    "Starts the continuous differential conversion mode and returns the first ADC reading \
    in mV as the difference from the specified channels. \
    The sps controls the sample rate. \
    The pga must be given in mV, see datasheet page 13 for the supported values. \
    Use getLastConversionResults() to read the next values and \
    stopContinuousConversion() to stop converting."

    # Disable comparator, Non-latching, Alert/Rdy active low
    # traditional comparator, continuous mode
    # The last flag is the only change we need, page 11 datasheet
    config = self.__ADS1015_REG_CONFIG_CQUE_NONE    | \
             self.__ADS1015_REG_CONFIG_CLAT_NONLAT  | \
             self.__ADS1015_REG_CONFIG_CPOL_ACTVLOW | \
             self.__ADS1015_REG_CONFIG_CMODE_TRAD   | \
             self.__ADS1015_REG_CONFIG_MODE_CONTIN

    # Set sample per seconds, defaults to 250sps
    # If sps is in the dictionary (defined in init()) it returns the value of the constant
    # othewise it returns the value for 250sps. This saves a lot of if/elif/else code!
    if (self.ic == self.__IC_ADS1015):
      config |= self.spsADS1015.setdefault(sps, self.__ADS1015_REG_CONFIG_DR_1600SPS)
    else:
      if ( (sps not in self.spsADS1115) & self.debug):
	print "ADS1x15: Invalid pga specified: %d, using 6144mV" % sps
      config |= self.spsADS1115.setdefault(sps, self.__ADS1115_REG_CONFIG_DR_250SPS)

    # Set PGA/voltage range, defaults to +-6.144V
    if ( (pga not in self.pgaADS1x15) & self.debug):
      print "ADS1x15: Invalid pga specified: %d, using 6144mV" % sps
    config |= self.pgaADS1x15.setdefault(pga, self.__ADS1015_REG_CONFIG_PGA_6_144V)
    self.pga = pga

    # Set channels
    if ( (chP == 0) & (chN == 1) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_0_1
    elif ( (chP == 0) & (chN == 3) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_0_3
    elif ( (chP == 2) & (chN == 3) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_2_3
    elif ( (chP == 1) & (chN == 3) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_1_3
    else:
      if (self.debug):
	print "ADS1x15: Invalid channels specified: %d, %d" % (chP, chN)
	return -1

    # Set 'start single-conversion' bit to begin conversions
    # No need to change this for continuous mode!
    config |= self.__ADS1015_REG_CONFIG_OS_SINGLE

    # Write config register to the ADC
    # Once we write the ADC will convert continously
    # we can read the next values using getLastConversionResult
    bytes = [(config >> 8) & 0xFF, config & 0xFF]
    self.i2c.writeList(self.__ADS1015_REG_POINTER_CONFIG, bytes)

    # Wait for the ADC conversion to complete
    # The minimum delay depends on the sps: delay >= 1/sps
    # We add 0.5ms to be sure
    delay = 1.0/sps+0.0005
    time.sleep(delay)

    # Read the conversion results
    result = self.i2c.readList(self.__ADS1015_REG_POINTER_CONVERT, 2)
    if (self.ic == self.__IC_ADS1015):
	# Shift right 4 bits for the 12-bit ADS1015 and convert to mV
	return ( ((result[0] << 8) | (result[1] & 0xFF)) >> 4 )*pga/2048.0
    else:
	# Return a mV value for the ADS1115
	# (Take signed values into account as well)
	val = (result[0] << 8) | (result[1])
	if val > 0x7FFF:
	  return (val - 0xFFFF)*pga/32768.0
	else:
	  return ( (result[0] << 8) | (result[1]) )*pga/32768.0


  def stopContinuousConversion(self):
    "Stops the ADC's conversions when in continuous mode \
    and resets the configuration to its default value."
    # Write the default config register to the ADC
    # Once we write, the ADC will do a single conversion and
    # enter power-off mode.
    config = 0x8583 # Page 18 datasheet.
    bytes = [(config >> 8) & 0xFF, config & 0xFF]
    self.i2c.writeList(self.__ADS1015_REG_POINTER_CONFIG, bytes)
    return True

  def getLastConversionResults(self):
    "Returns the last ADC conversion result in mV"
    # Read the conversion results
    result = self.i2c.readList(self.__ADS1015_REG_POINTER_CONVERT, 2)
    if (self.ic == self.__IC_ADS1015):
    	# Shift right 4 bits for the 12-bit ADS1015 and convert to mV
    	return ( ((result[0] << 8) | (result[1] & 0xFF)) >> 4 )*self.pga/2048.0
    else:
	# Return a mV value for the ADS1115
	# (Take signed values into account as well)
	val = (result[0] << 8) | (result[1])
	if val > 0x7FFF:
	  return (val - 0xFFFF)*self.pga/32768.0
	else:
	  return ( (result[0] << 8) | (result[1]) )*self.pga/32768.0


  def startSingleEndedComparator(self, channel, thresholdHigh, thresholdLow, \
                                 pga=6144, sps=250, \
                                 activeLow=True, traditionalMode=True, latching=False, \
                                 numReadings=1):
    "Starts the comparator mode on the specified channel, see datasheet pg. 15. \
    In traditional mode it alerts (ALERT pin will go low)  when voltage exceeds  \
    thresholdHigh until it falls below thresholdLow (both given in mV). \
    In window mode (traditionalMode=False) it alerts when voltage doesn't lie\
    between both thresholds.\
    In latching mode the alert will continue until the conversion value is read. \
    numReadings controls how many readings are necessary to trigger an alert: 1, 2 or 4.\
    Use getLastConversionResults() to read the current value  (which may differ \
    from the one that triggered the alert) and clear the alert pin in latching mode. \
    This function starts the continuous conversion mode.  The sps controls \
    the sample rate and the pga the gain, see datasheet page 13. "

    # With invalid channel return -1
    if (channel > 3):
      if (self.debug):
	print "ADS1x15: Invalid channel specified: %d" % channel
      return -1

    # Continuous mode
    config = self.__ADS1015_REG_CONFIG_MODE_CONTIN

    if (activeLow==False):
      config |= self.__ADS1015_REG_CONFIG_CPOL_ACTVHI
    else:
      config |= self.__ADS1015_REG_CONFIG_CPOL_ACTVLOW

    if (traditionalMode==False):
      config |= self.__ADS1015_REG_CONFIG_CMODE_WINDOW
    else:
      config |= self.__ADS1015_REG_CONFIG_CMODE_TRAD

    if (latching==True):
      config |= self.__ADS1015_REG_CONFIG_CLAT_LATCH
    else:
      config |= self.__ADS1015_REG_CONFIG_CLAT_NONLAT

    if (numReadings==4):
      config |= self.__ADS1015_REG_CONFIG_CQUE_4CONV
    elif (numReadings==2):
      config |= self.__ADS1015_REG_CONFIG_CQUE_2CONV
    else:
      config |= self.__ADS1015_REG_CONFIG_CQUE_1CONV

    # Set sample per seconds, defaults to 250sps
    # If sps is in the dictionary (defined in init()) it returns the value of the constant
    # othewise it returns the value for 250sps. This saves a lot of if/elif/else code!
    if (self.ic == self.__IC_ADS1015):
      if ( (sps not in self.spsADS1015) & self.debug):
	print "ADS1x15: Invalid sps specified: %d, using 1600sps" % sps
      config |= self.spsADS1015.setdefault(sps, self.__ADS1015_REG_CONFIG_DR_1600SPS)
    else:
      if ( (sps not in self.spsADS1115) & self.debug):
	print "ADS1x15: Invalid sps specified: %d, using 250sps" % sps
      config |= self.spsADS1115.setdefault(sps, self.__ADS1115_REG_CONFIG_DR_250SPS)

    # Set PGA/voltage range, defaults to +-6.144V
    if ( (pga not in self.pgaADS1x15) & self.debug):
      print "ADS1x15: Invalid pga specified: %d, using 6144mV" % pga
    config |= self.pgaADS1x15.setdefault(pga, self.__ADS1015_REG_CONFIG_PGA_6_144V)
    self.pga = pga

    # Set the channel to be converted
    if channel == 3:
      config |= self.__ADS1015_REG_CONFIG_MUX_SINGLE_3
    elif channel == 2:
      config |= self.__ADS1015_REG_CONFIG_MUX_SINGLE_2
    elif channel == 1:
      config |= self.__ADS1015_REG_CONFIG_MUX_SINGLE_1
    else:
      config |= self.__ADS1015_REG_CONFIG_MUX_SINGLE_0

    # Set 'start single-conversion' bit to begin conversions
    config |= self.__ADS1015_REG_CONFIG_OS_SINGLE

    # Write threshold high and low registers to the ADC
    # V_digital = (2^(n-1)-1)/pga*V_analog
    if (self.ic == self.__IC_ADS1015):
      thresholdHighWORD = int(thresholdHigh*(2048.0/pga))
    else:
      thresholdHighWORD = int(thresholdHigh*(32767.0/pga))
    bytes = [(thresholdHighWORD >> 8) & 0xFF, thresholdHighWORD & 0xFF]
    self.i2c.writeList(self.__ADS1015_REG_POINTER_HITHRESH, bytes)

    if (self.ic == self.__IC_ADS1015):
      thresholdLowWORD = int(thresholdLow*(2048.0/pga))
    else:
      thresholdLowWORD = int(thresholdLow*(32767.0/pga))
    bytes = [(thresholdLowWORD >> 8) & 0xFF, thresholdLowWORD & 0xFF]
    self.i2c.writeList(self.__ADS1015_REG_POINTER_LOWTHRESH, bytes)

    # Write config register to the ADC
    # Once we write the ADC will convert continously and alert when things happen,
    # we can read the converted values using getLastConversionResult
    bytes = [(config >> 8) & 0xFF, config & 0xFF]
    self.i2c.writeList(self.__ADS1015_REG_POINTER_CONFIG, bytes)


  def startDifferentialComparator(self, chP, chN, thresholdHigh, thresholdLow, \
                                 pga=6144, sps=250, \
                                 activeLow=True, traditionalMode=True, latching=False, \
                                 numReadings=1):
    "Starts the comparator mode on the specified channel, see datasheet pg. 15. \
    In traditional mode it alerts (ALERT pin will go low)  when voltage exceeds  \
    thresholdHigh until it falls below thresholdLow (both given in mV). \
    In window mode (traditionalMode=False) it alerts when voltage doesn't lie\
    between both thresholds.\
    In latching mode the alert will continue until the conversion value is read. \
    numReadings controls how many readings are necessary to trigger an alert: 1, 2 or 4.\
    Use getLastConversionResults() to read the current value  (which may differ \
    from the one that triggered the alert) and clear the alert pin in latching mode. \
    This function starts the continuous conversion mode.  The sps controls \
    the sample rate and the pga the gain, see datasheet page 13. "

    # Continuous mode
    config = self.__ADS1015_REG_CONFIG_MODE_CONTIN

    if (activeLow==False):
      config |= self.__ADS1015_REG_CONFIG_CPOL_ACTVHI
    else:
      config |= self.__ADS1015_REG_CONFIG_CPOL_ACTVLOW

    if (traditionalMode==False):
      config |= self.__ADS1015_REG_CONFIG_CMODE_WINDOW
    else:
      config |= self.__ADS1015_REG_CONFIG_CMODE_TRAD

    if (latching==True):
      config |= self.__ADS1015_REG_CONFIG_CLAT_LATCH
    else:
      config |= self.__ADS1015_REG_CONFIG_CLAT_NONLAT

    if (numReadings==4):
      config |= self.__ADS1015_REG_CONFIG_CQUE_4CONV
    elif (numReadings==2):
      config |= self.__ADS1015_REG_CONFIG_CQUE_2CONV
    else:
      config |= self.__ADS1015_REG_CONFIG_CQUE_1CONV

    # Set sample per seconds, defaults to 250sps
    # If sps is in the dictionary (defined in init()) it returns the value of the constant
    # othewise it returns the value for 250sps. This saves a lot of if/elif/else code!
    if (self.ic == self.__IC_ADS1015):
      if ( (sps not in self.spsADS1015) & self.debug):
	print "ADS1x15: Invalid sps specified: %d, using 1600sps" % sps
      config |= self.spsADS1015.setdefault(sps, self.__ADS1015_REG_CONFIG_DR_1600SPS)
    else:
      if ( (sps not in self.spsADS1115) & self.debug):
	print "ADS1x15: Invalid sps specified: %d, using 250sps" % sps
      config |= self.spsADS1115.setdefault(sps, self.__ADS1115_REG_CONFIG_DR_250SPS)

    # Set PGA/voltage range, defaults to +-6.144V
    if ( (pga not in self.pgaADS1x15) & self.debug):
      print "ADS1x15: Invalid pga specified: %d, using 6144mV" % pga
    config |= self.pgaADS1x15.setdefault(pga, self.__ADS1015_REG_CONFIG_PGA_6_144V)
    self.pga = pga

    # Set channels
    if ( (chP == 0) & (chN == 1) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_0_1
    elif ( (chP == 0) & (chN == 3) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_0_3
    elif ( (chP == 2) & (chN == 3) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_2_3
    elif ( (chP == 1) & (chN == 3) ):
      config |= self.__ADS1015_REG_CONFIG_MUX_DIFF_1_3
    else:
      if (self.debug):
	print "ADS1x15: Invalid channels specified: %d, %d" % (chP, chN)
	return -1

    # Set 'start single-conversion' bit to begin conversions
    config |= self.__ADS1015_REG_CONFIG_OS_SINGLE

    # Write threshold high and low registers to the ADC
    # V_digital = (2^(n-1)-1)/pga*V_analog
    if (self.ic == self.__IC_ADS1015):
      thresholdHighWORD = int(thresholdHigh*(2048.0/pga))
    else:
      thresholdHighWORD = int(thresholdHigh*(32767.0/pga))
    bytes = [(thresholdHighWORD >> 8) & 0xFF, thresholdHighWORD & 0xFF]
    self.i2c.writeList(self.__ADS1015_REG_POINTER_HITHRESH, bytes)

    if (self.ic == self.__IC_ADS1015):
      thresholdLowWORD = int(thresholdLow*(2048.0/pga))
    else:
      thresholdLowWORD = int(thresholdLow*(32767.0/pga))
    bytes = [(thresholdLowWORD >> 8) & 0xFF, thresholdLowWORD & 0xFF]
    self.i2c.writeList(self.__ADS1015_REG_POINTER_LOWTHRESH, bytes)

    # Write config register to the ADC
    # Once we write the ADC will convert continously and alert when things happen,
    # we can read the converted values using getLastConversionResult
    bytes = [(config >> 8) & 0xFF, config & 0xFF]
    self.i2c.writeList(self.__ADS1015_REG_POINTER_CONFIG, bytes)
*/
}


object Ads1x15 {
  // Pointer Register
  val __ADS1015_REG_POINTER_MASK        = 0x03
  val __ADS1015_REG_POINTER_CONVERT     = 0x00
  val __ADS1015_REG_POINTER_CONFIG      = 0x01
  val __ADS1015_REG_POINTER_LOWTHRESH   = 0x02
  val __ADS1015_REG_POINTER_HITHRESH    = 0x03

  // Config Register
  val __ADS1015_REG_CONFIG_OS_MASK      = 0x8000
  val __ADS1015_REG_CONFIG_OS_SINGLE    = 0x8000  // Write: Set to start a single-conversion
  val __ADS1015_REG_CONFIG_OS_BUSY      = 0x0000  // Read: Bit = 0 when conversion is in progress
  val __ADS1015_REG_CONFIG_OS_NOTBUSY   = 0x8000  // Read: Bit = 1 when device is not performing a conversion

  val __ADS1015_REG_CONFIG_MUX_MASK     = 0x7000
  val __ADS1015_REG_CONFIG_MUX_DIFF_0_1 = 0x0000  // Differential P = AIN0, N = AIN1 (default)
  val __ADS1015_REG_CONFIG_MUX_DIFF_0_3 = 0x1000  // Differential P = AIN0, N = AIN3
  val __ADS1015_REG_CONFIG_MUX_DIFF_1_3 = 0x2000  // Differential P = AIN1, N = AIN3
  val __ADS1015_REG_CONFIG_MUX_DIFF_2_3 = 0x3000  // Differential P = AIN2, N = AIN3
  val __ADS1015_REG_CONFIG_MUX_SINGLE_0 = 0x4000  // Single-ended AIN0
  val __ADS1015_REG_CONFIG_MUX_SINGLE_1 = 0x5000  // Single-ended AIN1
  val __ADS1015_REG_CONFIG_MUX_SINGLE_2 = 0x6000  // Single-ended AIN2
  val __ADS1015_REG_CONFIG_MUX_SINGLE_3 = 0x7000  // Single-ended AIN3

  val __ADS1015_REG_CONFIG_PGA_MASK     = 0x0E00
  val __ADS1015_REG_CONFIG_PGA_6_144V   = 0x0000  // +/-6.144V range
  val __ADS1015_REG_CONFIG_PGA_4_096V   = 0x0200  // +/-4.096V range
  val __ADS1015_REG_CONFIG_PGA_2_048V   = 0x0400  // +/-2.048V range (default)
  val __ADS1015_REG_CONFIG_PGA_1_024V   = 0x0600  // +/-1.024V range
  val __ADS1015_REG_CONFIG_PGA_0_512V   = 0x0800  // +/-0.512V range
  val __ADS1015_REG_CONFIG_PGA_0_256V   = 0x0A00  // +/-0.256V range

  val __ADS1015_REG_CONFIG_MODE_MASK    = 0x0100
  val __ADS1015_REG_CONFIG_MODE_CONTIN  = 0x0000  // Continuous conversion mode
  val __ADS1015_REG_CONFIG_MODE_SINGLE  = 0x0100  // Power-down single-shot mode (default)

  val __ADS1015_REG_CONFIG_DR_MASK      = 0x00E0
  val __ADS1015_REG_CONFIG_DR_128SPS    = 0x0000  // 128 samples per second
  val __ADS1015_REG_CONFIG_DR_250SPS    = 0x0020  // 250 samples per second
  val __ADS1015_REG_CONFIG_DR_490SPS    = 0x0040  // 490 samples per second
  val __ADS1015_REG_CONFIG_DR_920SPS    = 0x0060  // 920 samples per second
  val __ADS1015_REG_CONFIG_DR_1600SPS   = 0x0080  // 1600 samples per second (default)
  val __ADS1015_REG_CONFIG_DR_2400SPS   = 0x00A0  // 2400 samples per second
  val __ADS1015_REG_CONFIG_DR_3300SPS   = 0x00C0  // 3300 samples per second (also 0x00E0)

  val __ADS1115_REG_CONFIG_DR_8SPS      = 0x0000  // 8 samples per second
  val __ADS1115_REG_CONFIG_DR_16SPS     = 0x0020  // 16 samples per second
  val __ADS1115_REG_CONFIG_DR_32SPS     = 0x0040  // 32 samples per second
  val __ADS1115_REG_CONFIG_DR_64SPS     = 0x0060  // 64 samples per second
  val __ADS1115_REG_CONFIG_DR_128SPS    = 0x0080  // 128 samples per second
  val __ADS1115_REG_CONFIG_DR_250SPS    = 0x00A0  // 250 samples per second (default)
  val __ADS1115_REG_CONFIG_DR_475SPS    = 0x00C0  // 475 samples per second
  val __ADS1115_REG_CONFIG_DR_860SPS    = 0x00E0  // 860 samples per second

  val __ADS1015_REG_CONFIG_CMODE_MASK   = 0x0010
  val __ADS1015_REG_CONFIG_CMODE_TRAD   = 0x0000  // Traditional comparator with hysteresis (default)
  val __ADS1015_REG_CONFIG_CMODE_WINDOW = 0x0010  // Window comparator

  val __ADS1015_REG_CONFIG_CPOL_MASK    = 0x0008
  val __ADS1015_REG_CONFIG_CPOL_ACTVLOW = 0x0000  // ALERT/RDY pin is low when active (default)
  val __ADS1015_REG_CONFIG_CPOL_ACTVHI  = 0x0008  // ALERT/RDY pin is high when active

  val __ADS1015_REG_CONFIG_CLAT_MASK    = 0x0004  // Determines if ALERT/RDY pin latches once asserted
  val __ADS1015_REG_CONFIG_CLAT_NONLAT  = 0x0000  // Non-latching comparator (default)
  val __ADS1015_REG_CONFIG_CLAT_LATCH   = 0x0004  // Latching comparator

  val __ADS1015_REG_CONFIG_CQUE_MASK    = 0x0003
  val __ADS1015_REG_CONFIG_CQUE_1CONV   = 0x0000  // Assert ALERT/RDY after one conversions
  val __ADS1015_REG_CONFIG_CQUE_2CONV   = 0x0001  // Assert ALERT/RDY after two conversions
  val __ADS1015_REG_CONFIG_CQUE_4CONV   = 0x0002  // Assert ALERT/RDY after four conversions
  val __ADS1015_REG_CONFIG_CQUE_NONE    = 0x0003  // Disable the comparator and put ALERT/RDY in high state (default)

}