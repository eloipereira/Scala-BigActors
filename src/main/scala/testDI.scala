package bigactors

/**
 * Created with IntelliJ IDEA.
 * User: eloipereira
 * Date: 11/5/13
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
 */
object testDI extends App {
      // service interfaces
  trait OnOffDevice {
    def on: Unit
    def off: Unit
  }
  trait SensorDevice {
    def isCoffeePresent: Boolean
  }

  // service implementations
  class Heater extends OnOffDevice {
    def on = println("heater.on")
    def off = println("heater.off")
  }
  class PotSensor extends SensorDevice {
    def isCoffeePresent = false
  }

  // service declaring two dependencies that it wants injected
  class Warmer(
    implicit val sensor: SensorDevice,
    implicit val onOff: OnOffDevice) {

    def trigger = {
      if (sensor.isCoffeePresent) onOff.on
      else onOff.off
    }
  }

  // module binding dependencies
  trait ServicesModule {
    protected implicit lazy val potSensor: SensorDevice =
      new PotSensor
    protected implicit lazy val heater: OnOffDevice =
      new Heater
  }

  // compose an injector from modules
  class AppInjector extends ServicesModule {
    implicit lazy val warmer = new Warmer
  }

  // use injector. the wiring is done automatically using
  // the implicits
  new AppInjector().warmer.trigger
}
