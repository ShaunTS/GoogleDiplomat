package sts.libs

import java.lang.Throwable
import scala.util.{Failure, Success, Try}
import scalaz.{-\/, \/, \/-}
import sts.libs.errors._
import sts.libs.functional.PiecewiseFunction

trait GenErrorMap {

    val nomatch = PartialFunction[Throwable, GenError] {
        case e: Throwable => MiscThrown(e)
    }

    def errors: List[PartialFunction[Throwable, GenError]]

    lazy val throwableMap = PiecewiseFunction[Throwable, GenError](this.errors: _ *)

    def apply(e: Throwable): GenError = (throwableMap :+ nomatch)(e)
}


trait GenericHandler[E <: GenErrorMap] {

    def errorMap: E

    def apply[T](block: => T): \/[GenError, T] = Try { block } match {
        case Success(s) => \/-(s)
        case Failure(e) => -\/(errorMap(e))
    }


    def nested[T](block: => \/[GenError, T]) = Try { block } match {
        case Success(result: \/-[T]) => result
        case Success(result: -\/[GenError]) => result
        case Failure(e) => -\/(errorMap(e))
    }
}
