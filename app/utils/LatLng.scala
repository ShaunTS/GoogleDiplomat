package sts.diplomat.util

import sts.diplomat.models._


/** CONTENTS (WORK IN PROGRESS)
 *
 *  Utilities for representing and converting between various units of physical
 *  distance.
 */


object LatLng {

    /** TODO
     * Estimates the physical distance between two points represented
     * as geocoordinates.
     */
    def distance(pntA: Coords, pntB: Coords): Double = {

        ???
    }
}


abstract class Dist(x: Double) {

    def toFeetDbl: Double

    def toMilesDbl: Double

    def toMetersDbl: Double

    def toKilometersDbl: Double
}

class AbstractMeter(xn: Double) extends Dist(xn) {

    def x: Double = this.xn

    def toFeetDbl: Double = (this.x * 3.28084D)

    def toMilesDbl: Double = (this.toFeetDbl / 5280D)

    def toMetersDbl: Double = this.x

    def toKilometersDbl: Double = this.decreaseByFactor(3).x

    def increaseByFactor(n: Int): AbstractMeter = {
        new AbstractMeter(this.x * n * 10)
    }

    def decreaseByFactor(n: Int): AbstractMeter = {
        new AbstractMeter(this.x / n*10)
    }
}
object AbstractMeter {

    def apply(x: Double): AbstractMeter = {
        new AbstractMeter(x)
    }
}

case class Meter(m: Double) extends AbstractMeter(m)

