package sts.play.json.helpers

import play.api.libs.json._
import play.api.data.validation.ValidationError
import scala.util.{Failure, Success, Try}

package object ReadAsString {

    /**
     *  Attempts to read a json value as a double, if this fails the value will
     *  be read as a String and an attempt is made to parse a double from it.
     */
    def toDouble: Reads[Double] = new Reads[Double] {

        def reads(jsv: JsValue): JsResult[Double] = {
            Reads.DoubleReads.reads(jsv).orElse {
                Reads.StringReads.reads(jsv).flatMap(tryParse)
            }
        }


        def tryParse(dblAsString: String): JsResult[Double] =
            Try(dblAsString.toDouble) match {
                case Success(dbl) => JsSuccess(dbl)
                case Failure(e) => error.MakeJsError(
                    s"Could not parse Double (${e.getMessage}) when reading from JSON."
                )
            }
    }
}

package object error {

    case class JsErrorInfo(path: JsPath, message: String)

    object JSEInfo {

        def apply(jse: JsError): JsErrorInfo = jse.errors.headOption.map {
            case (path, (v::vs)) => JsErrorInfo(path, v.message)
        }.getOrElse(JsErrorInfo(JsPath(), ""))
    }

    object MakeJsPath {

        def apply(stringPath: String): JsPath = stringPath match {
            case "" => JsPath()
            case _ => JsPath(List(KeyPathNode(stringPath)))
        }
    }

    object MakeJsError {

        def apply(msg: String, path: JsPath = JsPath()) = JsError(
            Seq(path -> Seq(ValidationError(Seq(msg))))
        )
    }
}