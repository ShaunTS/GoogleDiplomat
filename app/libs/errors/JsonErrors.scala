package sts.libs.errors

import com.fasterxml.jackson.core.JsonProcessingException
import java.lang.Throwable
import play.api.data.validation.ValidationError
import play.api.libs.json._
import scalaz.{-\/, \/, \/-}
import sts.libs.functional.{FunctionFragment, PiecewiseFunction}


trait JsonPlayError extends GenError {

    def playError: JsError

    def source: \/[String, JsValue]

    def withSource(src: String): JsonPlayError

    def withSource(src: JsValue): JsonPlayError

    def showSrc: String = this.source.fold(
        {
            case src if(src.nonEmpty) => src
            case _ => "{}"
        },
        Json.prettyPrint(_)
    )
}

object PlayJsError {

    def findMessage(jse: JsError): String = jse.errors.headOption.map {
        case (_, (v::vs)) => v.message
    }.getOrElse("")
}

case class JsonMissingPath(
    message: String,
    path: JsPath,
    playError: JsError,
    source: \/[String, JsValue] = -\/("")
) extends JsonPlayError {

    def cause = None

    def withSource(src: String) = this.copy(source = -\/(src))

    def withSource(src: JsValue) = this.copy(source = \/-(src))
}
object JsonMissingPath {

    def diagnose = PartialFunction[JsError, JsonMissingPath] {
        case jse if(PlayJsError.findMessage(jse) equals "error.path.missing") => apply(jse)
    }

    def apply(jse: JsError): JsonMissingPath = {
        val (path, invalids) = jse.errors.head

        JsonMissingPath(invalids.take(1).mkString, path, jse)
    }
}

case class JsonUnexpectedType(
    message: String,
    path: JsPath,
    playError: JsError,
    source: \/[String, JsValue] = -\/("")
) extends JsonPlayError {

    def cause = None

    def withSource(src: String) = this.copy(source = -\/(src))

    def withSource(src: JsValue) = this.copy(source = \/-(src))
}
object JsonUnexpectedType {

    def diagnose = PartialFunction[JsError, JsonUnexpectedType] {
        case jse if(PlayJsError.findMessage(jse) containsSlice "error.expected") => apply(jse)
    }

    def apply(jse: JsError): JsonUnexpectedType = {
        val (path, invalids) = jse.errors.head

        JsonUnexpectedType(invalids.take(1).mkString, path, jse)
    }
}

case class JsonParseError(
    message: String,
    cause: Option[Throwable],
    source: \/[String, JsValue] = -\/(""),
    line: Int = 0,
    colA: Option[Int] = None,
    colB: Option[Int] = None
) extends JsonPlayError {

    def playError = JsError(
        JsPath(List(KeyPathNode(""))), this.message
    )

    def withSource(src: String) = this.copy(source = -\/(src))

    def withSource(src: JsValue) = this.copy(source = \/-(src))

    def withSourceObj(json: Object): JsonParseError = json match {
        case js: String => this.copy(source = -\/(js))
        case _ => this
    }
}
object JsonParseError {

    def diagnose = PartialFunction[Throwable, JsonParseError] {
        case e: JsonProcessingException => apply(e)
    }

    def apply(e: JsonProcessingException): JsonParseError = {

        e.getLocation.getSourceRef match {
            case null => JsonParseError(e.getMessage, Some(e))
            case json => full(e)
        }
    }

    val colR = """.* column: ([0-9]+).*""".r

    def full(e: JsonProcessingException): JsonParseError = {
        val msg = e.getMessage.split("\n").head.trim
        val loc = e.getLocation

        val colA = msg match {
            case colR(cl) => Some(cl.toInt)
            case _ => None
        }

        JsonParseError(
            message = msg,
            cause = Some(e),
            line = loc.getLineNr,
            colA = colA,
            colB = Some(loc.getColumnNr)
        ).withSourceObj(loc.getSourceRef)
    }
}