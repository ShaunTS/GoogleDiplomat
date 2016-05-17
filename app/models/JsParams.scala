package sts.libs.json

import play.api.libs.functional.syntax._
import play.api.libs.json._
import scala.reflect.ClassTag
import scalaz.{-\/, \/, \/-}
import sts.libs.errors.{GenError, JsonPlayError, JsMissingRequiredField, JsonUnexpectedType}
import sts.libs.JsonOps
import sts.util.Jsonables._


case class JsParam[A](
    key: String,
    value: Option[A],
    required: Boolean = false
)(
    implicit read: Reads[A], write: Writes[A], tag: ClassTag[A]
) extends JsonOps {

    val rd: Reads[A] = read
    val wrt: Writes[A] = write
    val ct: ClassTag[A] = tag

    def require: JsParam[A] = this.copy(required = true)

    def toJsonOpt: Option[JsObject] = this.value.map { paramVal =>
        Json.obj(this.key -> this.wrt.writes(paramVal))
    }

    def toJson: JsObject = this.toJsonOpt.getOrElse {
        Json.obj(this.key -> JsNull)
    }

    def stringify: String = Json.stringify(this.toJson)

    def ifRequired(e: GenError): GenError = (e -> this.required) match {
        case (jse: JsonPlayError, true) => JsMissingRequiredField(jse, this.key)
        case _ => e
    }

    def innerJson(outer: JsValue): \/[GenError, JsValue] = JsonHandler.fromJson(outer) {
        (__ \ this.key).read[JsValue]
    }

    def fromJson(json: JsValue): \/[GenError, JsParam[A]] = {
        (for {
            inner <- innerJson(json)
            jsp <- JsonHandler.fromJson(inner)(this.rd)
        } yield jsp ).map {
            case obj: A => this.withValue(obj)
        }.leftMap(ifRequired)
    }

    def withValue(value: A): JsParam[A] = this.copy(value = Some(value))

    def isEmpty: Boolean = this.value.isEmpty

    def nonEmpty: Boolean = this.value.nonEmpty
}
object JsParam {

    implicit lazy val writes: Writes[JsParam[_]] = new Writes[JsParam[_]] {

        def writes(jsp: JsParam[_]) = jsp.toJson
    }

    def apply[A]( pair: (String, A) )(implicit rd: Reads[A], wrt: Writes[A], ct: ClassTag[A]): JsParam[A] =
        JsParam(pair._1, Some(pair._2))(rd, wrt, ct)

    def empty[A](key: String)(implicit rd: Reads[A], wrt: Writes[A], ct: ClassTag[A]): JsParam[A] =
        JsParam(key, Option.empty[A])(rd, wrt, ct)
}

abstract class JsParamList[L] {

    def params: Seq[JsParam[_]]

    def flatten: Seq[JsParam[_]] = params.filter(_.nonEmpty)

    def apply[A: ClassTag](key: String): Option[A] =
        this.params.collectFirst {
            case JsParam(`key`, Some(value), _) => value
        }.collect {
            case value: A => value
        }

    lazy val keys: Seq[String] = this.params.map(_.key)

    def mergeValues(values: Seq[JsParam[_]]): Seq[JsParam[_]] = {

        val toAdd: Seq[JsParam[_]] = values.toSeq.filter {
            case JsParam(key, _, _) => this.keys.contains(key)
        }

        val filtered: Seq[JsParam[_]] = this.params.filterNot {
            case JsParam(key, _, _) => toAdd.exists(_.key == key)
        }

        (toAdd ++ filtered).sortBy(_.key)
    }

    def withValues(params: JsParam[_] *): L

    def toJson: JsObject = this.params.flatMap(_.toJsonOpt)
        .fold(JsObject(Nil)) { (a, b) => a ++ b }

    def stringify: String = Json.stringify(this.toJson)

    def stringOpt: Option[String] = Some(Json.stringify(this.toJson))
        .filterNot(_ == "{}")

}
abstract class JsParamLists[L <: JsParamList[L]] extends JsonOps {

    def apply(p: Seq[JsParam[_]]): L

    def defaultKeys: Seq[JsParam[_]]

    def default: L = apply(defaultKeys)

    def parse(opt: Option[String]): L = opt.map(parse).getOrElse(this.default)

    def parse(json: String): L =
    (for {
        jsValue <- JsonHandler.parse(json)
        list <- JsonHandler.fromJson[L](jsValue)
    } yield list).getOrElse(this.default)

    implicit def writes: Writes[L] = new Writes[L] {

        def writes(jspList: L) = jspList.toJson
    }

    implicit def reads: Reads[L] = new Reads[L] {

        def reads(json: JsValue): JsResult[L] = {

            val jsResults: Seq[ \/[GenError, JsParam[_]] ] = defaultKeys.map(_ fromJson json)

            val jsParams: Seq[JsParam[_]] = jsResults.collect {
                case \/-(jsp) => jsp
            }

            jsResults.collectFirst {
                case -\/(e: JsonUnexpectedType) => e.withSource(json)
                case -\/(e: JsMissingRequiredField) => e.withSource(json)
            } match {
                case Some(failed) => failed.toPlayError
                case _ => JsSuccess(apply(jsParams), JsPath(List(KeyPathNode(""))))
            }
        }
    }
}