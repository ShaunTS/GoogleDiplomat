package sts.libs.errors

import java.lang.Throwable
import scala.util.{Failure, Success, Try}
import scalaz.{-\/, \/, \/-}
import sts.util.debug.helpers._
import sts.libs.functional.{FunctionFragment, PiecewiseFunction}
import play.api.libs.json.{JsError, JsValue, JsResult, JsSuccess, Reads}


case class NoIdeaException(
    message: String = "Your guess is as good as mine."
) extends Throwable

trait GenError {

    def message: String
    def cause: Option[Throwable]

    def getCause: Throwable = this.cause.getOrElse(NoIdeaException())
}

trait Diagnosed {

    def diagnose: PartialFunction[Throwable, GenError]
}

case class MiscError(message: String) extends GenError {
    def cause = None
}

case class MiscThrown(message: String, cause: Option[Throwable]) extends GenError
object MiscThrown extends Diagnosed {

    def diagnose = PartialFunction[Throwable, GenError] {
        case e: Throwable => apply(e)
    }

    def apply(e: Throwable): MiscThrown = {
        MiscThrown(e.getMessage, Some(e))
    }
}
