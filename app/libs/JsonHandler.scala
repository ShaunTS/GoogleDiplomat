package sts.libs


import java.lang.Throwable
import play.api.libs.json.{JsError, JsValue, Json, JsSuccess, Reads}
import scalaz.{-\/, \/, \/-}
import sts.libs.errors._
import sts.libs.functional.PiecewiseFunction

class JsonErrorMap extends GenErrorMap {

    val errors = List(JsonParseError.diagnose, nomatch)

    def jsErrorMap = PiecewiseFunction[JsError, JsonPlayError](
        JsonMissingPath.diagnose,
        JsonUnexpectedType.diagnose,
        MiscJsonError.diagnose
    )

    def apply(jse: JsError): GenError = jsErrorMap(jse)

    def apply(jse: JsError, json: JsValue): GenError = jsErrorMap(jse).withSource(json)
}

class JsonFunctions(val errorMap: JsonErrorMap) extends GenericHandler[JsonErrorMap] {

    def parse(input: String): \/[GenError, JsValue] = apply { Json.parse(input) }

    def fromJson[T](json: JsValue)(implicit rd: Reads[T]): \/[GenError, T] = nested {
        rd.reads(json) match {
            case JsSuccess(js, _) => \/-(js)
            case e: JsError => -\/(errorMap(e, json))
        }
    }
}

trait JsonOps {

    def errorMap: JsonErrorMap = new JsonErrorMap()

    lazy val JsonHandler = new JsonFunctions(errorMap)
}