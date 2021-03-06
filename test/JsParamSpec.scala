package test.unit.libs.json


import org.specs2.matcher.DisjunctionMatchers
import org.specs2.mutable._
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads, Writes, Json, JsObject, JsError}
import play.api.test._
import play.api.test.Helpers._
import scalaz.{-\/, \/, \/-}
import sts.libs.errors.{GenError, JsonMissingPath}
import sts.play.json.helpers.error.{JsErrorInfo, JSEInfo => Info}
import sts.libs.json.{JsParam, JsParamList, JsParamLists}


class JsParamSpec extends Specification with DisjunctionMatchers {

    import JsParamTestData._

t
    "JsParam" should {

        tag("toJsonOpt")
        "Construct a new JsParam, and convert to Json" in {

            val jsp = JsParam("junk" -> j1)
            val stringJson = jsp.toJsonOpt.map(Json.stringify(_))

            stringJson must beSome("""{"junk":{"id":1,"name":"pizza","rank":1}}""")
        }

        "Un-wrap JsParam json, returning internal json representing the wrapped object" in {

            val testJson: JsObject = Json.obj(
                "garb" -> Json.obj(
                    "id" -> 2,
                    "name" -> "lama",
                    "rank" -> 3.2
                )
            )

            val jsp = JsParam.empty[Junk]("garb")

            val lamaJson = jsp.innerJson(testJson)

            lamaJson.map(Json.stringify) must be_\/-("""{"id":2,"name":"lama","rank":3.2}""")
        }

        "Construct an empty JsParam, and convert to JsNull" in {
            val jsp = JsParam.empty[Junk]("garb")

            val stringJson = Json.stringify(jsp.toJson)

            stringJson must equalTo("""{"garb":null}""")
        }

        "Read a JsParam from json, and return an instance of the wrapped class" in {
            val jsp = JsParam.empty[Junk]("j2")

            val json = Json.parse("""{"j2":{"id":2,"name":"pizza","rank":2}}""")

            val jsDisj: \/[GenError, JsParam[Junk]] = jsp.fromJson(json)

            jsDisj must be_\/-

            val \/-(jspResult) = jsDisj

            jspResult.value must beSome.which { j2 =>
                j2.id must beSome(2L)
                j2.name must equalTo("pizza")
                j2.rank must be_==(2D)
            }
        }
    }

br
    "A JsParamList concrete class" should {

        "Construct an empty param list and convert it to an empty json object" in {

            val jspList = JunkParams.default
            val json = Json.toJson(jspList)

            Json.stringify(json) must equalTo("{}")
        }

        "Populate an empty param list and convert to json" in {

            val jspList = JunkParams.default.withValues(
                JsParam("thing" -> j1),
                JsParam("batchId" -> 2L),
                JsParam("orderBy" -> "name"),
                JsParam("types" -> List("mosque", "park", "casino"))
            )

            val json = Json.toJson(jspList)

            Json.stringify(json) must equalTo(
                """{"batchId":2,"orderBy":"name","thing":{"id":1,"name":"pizza","rank":1},"types":["mosque","park","casino"]}"""
            )
        }

        "Partially populate a param list and convert to json" in {
            val jspList = JunkParams.default.withValues(
                JsParam("thing" -> j1),
                JsParam("types" -> List("mosque", "park", "casino"))
            )

            val json = Json.toJson(jspList)

            Json.stringify(json) must equalTo(
                """{"thing":{"id":1,"name":"pizza","rank":1},"types":["mosque","park","casino"]}"""
            )
        }

        "Parse and read a full param list from json" in {
            val jsVal = Json.parse(testJsonString)

            val jspList = jsVal.as[JunkParams]

            jspList[Long]("batchId") must beSome(2L)
            jspList[String]("orderBy") must beSome("name")

            jspList[Junk]("thing") must beSome.which { junk =>
                junk.id must beSome(1L)
                junk.name must equalTo("dog")
                junk.rank must equalTo(1D)
            }

            jspList[List[String]]("types").toList.flatten must contain(
                exactly("mosque", "park", "casino")
            )
        }

        tag("required")
        "Return a JsError when a required field is missing/null/malformed in the json" in {
            val jsVals = List(missingRequired, nullRequired, malformRequired).map(Json.parse)

            val errors = jsVals.map(Json.fromJson[JunkParams]).collect {
                case jse: JsError => jse
            }

            errors must haveLength(3)

            val List(missingReq, nullReq, malformReq) = errors

            Info(missingReq).message must equalTo("""error.path.missing @ 'obj.thing' in {"batchId":3}""")

            Info(nullReq).message must equalTo(
                """error.path.missing @ 'obj.thing.rank' in {"batchId":4,"thing":null}"""
            )

            Info(malformReq).message must equalTo(
                """error.path.missing @ 'obj.thing.rank' in {"batchId":5,"thing":{"id":2}}"""
            )
        }
    }

}

object JsParamTestData {


    case class JunkParams(params: Seq[JsParam[_]]) extends JsParamList[JunkParams] {

        def withValues(p: JsParam[_] *): JunkParams = this.copy(params = this.mergeValues(p))
    }
    object JunkParams extends JsParamLists[JunkParams] {

        def defaultKeys = Seq(
            JsParam.empty[List[String]]("types"),
            JsParam.empty[String]("orderBy"),
            JsParam.empty[Long]("batchId").require,
            JsParam.empty[Junk]("thing").require
        )
    }

    case class Junk(
        id: Option[Long],
        name: String,
        rank: Double
    )
    object Junk {

        implicit val writes: Writes[Junk] = (
            (__ \ "id").writeNullable[Long] and
            (__ \ "name").write[String] and
            (__ \ "rank").write[Double]
        )(unlift(Junk.unapply))

        implicit val reads: Reads[Junk] = (
            (__ \ "id").readNullable[Long] and
            (__ \ "name").read[String] and
            (__ \ "rank").read[Double]
        )(Junk.apply _)

        def altReads(key: String): Reads[JsObject] =
            (__ \ key).read[JsObject]

    }

    val testJsonString =
        """{"batchId":2,"orderBy":"name","thing":{"id":1,"name":"dog","rank":1},"types":["mosque","park","casino"]}"""

    val missingRequired = """{"batchId":3}"""

    val nullRequired = """{"batchId":4,"thing":null}"""

    val malformRequired = """{"batchId":5,"thing":{"id":2}}"""

    val j1 = Junk(Some(1), "pizza", 1.0)
}
