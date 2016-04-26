package sts.libs


import java.lang.Throwable
import scala.util.{Failure, Success, Try}
import scalaz.{-\/, \/, \/-}
import sts.util.debug.helpers._
import sts.libs.functional.{FunctionFragment, PiecewiseFunction}
import sts.libs.errors._
import play.api.libs.json.{JsError, JsValue, Json, JsResult, JsSuccess, Reads}

trait Janitor {

    val nomatch = PartialFunction[Throwable, GenError] {
        case e: Throwable => MiscThrown(e)
    }

    def errors: List[PartialFunction[Throwable, GenError]]

    lazy val errMap = PiecewiseFunction[Throwable, GenError](this.errors: _ *)

    def apply[T](block: =>T): \/[GenError, T] = {
        Try { block } match {
            case Success(s) => \/-(s)
            case Failure(e) => -\/(errMap(e))
        }
    }

    def nested[T](block: => \/[GenError, T]) = Try { block } match {
        case Success(result: \/-[T]) => result
        case Success(result: -\/[GenError]) => result
        case Failure(e) => -\/(errMap(e))
    }
}

object CatchAll extends Janitor {

    val errors = List(
        JsonParseError.diagnose,
        UniqueKeyViolation.diagnose,
        MiscPSQLError.diagnose,
        MiscSQLError.diagnose,
        nomatch
    )
}


object JsonFunction extends Janitor {

    val errors = List(JsonParseError.diagnose, nomatch)

    val playJsonErrors = PiecewiseFunction[JsError, JsonPlayError](
        JsonMissingPath.diagnose,
        JsonUnexpectedType.diagnose
    )

    def parse(input: String): \/[GenError, JsValue] = apply { Json.parse(input) }

    def read[T](json: JsValue)(implicit rd: Reads[T]): \/[GenError, T] = nested {
        rd.reads(json) match {
            case JsSuccess(js, _) => \/-(js)
            case e: JsError => -\/(playJsonErrors(e).withSource(json))
        }
    }

}