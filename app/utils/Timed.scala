package sts.util

import scala.collection.mutable.{Map => MutaMap}



/** This trait can be mixed into any class or object, providing the ability to
 *  measure and display the time taken to execute arbitrary blocks of code.
 */
trait TimeKeeping {

    val timeKeeper = new TimeKeeper()

    def startTime(tag: String) { timeKeeper.start(tag) }

    def endTime(tag: String) { timeKeeper.stop(tag) }

    def timed[B](tag: String)(f: => B): B = {

        startTime(tag); val result: B = f; endTime(tag)
        result
    }

    def showTimes: Unit = timeKeeper.report
}



sealed trait TimeMetric

case class TeeZero(
    tag: String,
    t0: Long
) extends TimeMetric {

    def stop: Timed = Timed(this.tag, this.t0, System.nanoTime)
}
object TeeZero {

    def apply(tag: String): TeeZero = TeeZero(tag, System.nanoTime)
}

case class Timed(
    tag: String,
    t0: Long,
    t: Long
) extends TimeMetric {

    def elapsed: Long = this.t - this.t0

    def millis: Double = {
        this.elapsed / 1000000D
    }
}


class TimeKeeper(times: MutaMap[String, TimeMetric] = MutaMap[String, TimeMetric]()) {

    def start(tag: String) {
        this.times += (tag -> TeeZero(tag))
    }

    def stop(arg: String) {

        this.times.collectFirst {
            case (`arg`, datum @ TeeZero(_, _)) => datum.stop
        }.foreach {
            timed => this.times += (arg -> timed)
        }
    }

    def padFront[T](data: List[(String, T)]): List[(String, T)] = data match {

        case (one::_) =>
            val longest: Int = data.maxBy { case(tag, _) => tag.length}._1.length

            data.map {
                case(tag, datum) => tag.reverse.padTo(longest, ' ').reverse -> datum
            }

        case _ => data
    }

    def report {

        val datums = this.times.toList.collect {
            case (_, datum @ Timed(_, _, _)) => datum
        }.sortBy {
            case Timed(_, _, t) => t
        }.map {
            datum => datum.tag -> datum.millis
        }

        println("_" * 60)

        padFront(datums).map { case (tag, time) => s"  $tag:\t${time}ms" }.foreach(println)

        println("\n\n")
    }
}