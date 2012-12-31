#Raspberry Pi GPIO Acess

##Why

I want to be able to program my [Raspberry Pi](http://www.raspberrypi.org/) in Scala.

That includes:
* working with the i2c bus
* working with GPIO
* sending data to Cosm

##Credits

I used as sources of information and inspiration:

* Official Broadcom [documentation](http://www.raspberrypi.org/wp-content/uploads/2012/02/BCM2835-ARM-Peripherals.pdf)
  on BCM835 peripherals (this is the chip inside Raspberry Pi)

* Tutorial on [RPi Low-Level Peripherals](http://elinux.org/RPi_Low-level_peripherals)

* [pi4j project](http://pi4j.com/)

* [RPi.GPIO](http://pypi.python.org/pypi/RPi.GPIO)

* Java i2c [binding](http://www.raspberrypi.org/phpBB3/viewtopic.php?f=41&t=3308&start=25)
by Peter Simon <epnomis@gmail.com>. This was my inspiration to use JNA. Only one call (ioctl)
turned out to be neccesary.

* Adafruit Raspberry Pi Python [Code](https://github.com/adafruit/Adafruit-Raspberry-Pi-Python-Code.git) for
the parts from Adafruit.

* Chris Hatton's [note](http://www.chrishatton.org/archives/88) on GPIO port on the Raspberry Pi

* Ilya's [note](http://highlyscalable.wordpress.com/2012/02/02/direct-memory-access-in-java/) on Unsafe

* i2c-tools package (the source of python-smbus that Adafruit uses) as an example on how to use i2c-dev driver

* i2c_bcm2708 driver

* [jpachube](http://code.google.com/p/jpachube/source/browse/trunk/src/Pachube/Pachube.java)
