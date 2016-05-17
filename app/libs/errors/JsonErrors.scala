package sts.libs.errors

import com.fasterxml.jackson.core.JsonProcessingException
import java.lang.Throwable
import play.api.libs.json._
import scalaz.{-\/, \/, \/-}
import sts.libs.functional.PiecewiseFunction
import play.api.data.validation.ValidationError

trait JsonPlayError extends GenError {

    def playError: JsError

    def source: \/[String, JsValue]

    def path: JsPath

    def withSource(src: String): JsonPlayError

    def withSource(src: JsValue): JsonPlayError

    def mapPath(f: JsPath => JsPath): JsonPlayError

    def stringify: String = this.source.fold(identity, Json.stringify(_))

    def showSrc: String = this.source.fold(
        {
            case src if(src.nonEmpty) => src
            case _ => "{}"
        },
        Json.prettyPrint(_)
    )

    def toPlayError: JsError = {
        val details = List(
            Some(this.message),
            Some(this.path.toJsonString).filterNot(_ => this.path.toString.isEmpty).map(" @ '" + _ + "'"),
            Some(this.stringify).filterNot(_.isEmpty).map(" in " + _)
        ).flatten.mkString

        MakeJsError(details, this.path)
    }
}

case class MiscJsonError(
    message: String,
    path: JsPath,
    playError: JsError,
    source: \/[String, JsValue] = -\/("")
) extends JsonPlayError {

    def cause = None

    def withSource(src: String) = this.copy(source = -\/(src))

    def withSource(src: JsValue) = this.copy(source = \/-(src))

    def mapPath(f: JsPath => JsPath) = this.copy(path = f(this.path))
}
object MiscJsonError {

    def diagnose = PartialFunction[JsError, MiscJsonError] {
        case jse => apply(jse)
    }

    def apply(jse: JsError): MiscJsonError = {
        val JsErrorInfo(jsPath, msg) = Info(jse)

        MiscJsonError(msg, jsPath, jse)
    }
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

    def mapPath(f: JsPath => JsPath) = this.copy(path = f(this.path))
}
object JsonMissingPath {

    def diagnose = PartialFunction[JsError, JsonMissingPath] {
        case jse if(Info(jse).message containsSlice "error.path.missing") => apply(jse)
    }

    def apply(jse: JsError): JsonMissingPath = {
        val JsErrorInfo(path, message) = Info(jse)

        JsonMissingPath(message, path, jse)
    }
}

case class JsonUnexpectedType(
    message: String,
    path: JsPath,
    playError: JsError,
    expectedType: String,
    source: \/[String, JsValue] = -\/("")
) extends JsonPlayError {

    def cause = None

    def withSource(src: String) = this.copy(source = -\/(src))

    def withSource(src: JsValue) = this.copy(source = \/-(src))

    def mapPath(f: JsPath => JsPath) = this.copy(path = f(this.path))
}
object JsonUnexpectedType {

    def diagnose = PartialFunction[JsError, JsonUnexpectedType] {
        case jse if(Info(jse).message containsSlice "error.expected") => apply(jse)
    }

    def apply(jse: JsError): JsonUnexpectedType = {
        val JsErrorInfo(path, message) = Info(jse)

        JsonUnexpectedType(message, path, jse, message.replace("error.expected.", ""))
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

    val path = JsPath()

    def playError = JsError(
        MakeJsPath(""), this.message
    )

    def withSource(src: String) = this.copy(source = -\/(src))

    def withSource(src: JsValue) = this.copy(source = \/-(src))

    def mapPath(f: JsPath => JsPath) = this.copy()

    def withSourceObj(json: Object): JsonParseError = json match {
        case js: String => this.copy(source = -\/(js))
        case _ => this
    }

    override def toPlayError: JsError = MakeJsError(this.message)
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

case class JsMissingRequiredField(
    message: String,
    path: JsPath,
    playError: JsError,
    source: \/[String, JsValue] = -\/("")
) extends JsonPlayError {

    def cause = None

    def withSource(src: String) = this.copy(source = -\/(src))

    def withSource(src: JsValue) = this.copy(source = \/-(src))

    def mapPath(f: JsPath => JsPath) = this.copy(path = f(this.path))
}
object JsMissingRequiredField {

    def apply(jse: JsonPlayError, expectedPath: String): JsMissingRequiredField =
        JsMissingRequiredField(
            message = jse.message,
            playError = jse.playError,
            source = jse.source,
            path = List(MakeJsPath(expectedPath), jse.path).distinct
                .fold(JsPath())(_ ++ _)
        )
}


case class JsErrorInfo(path: JsPath, message: String)

object Info {

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