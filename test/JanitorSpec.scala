package test.unit.libs

import org.specs2.mutable._
import org.specs2.matcher.DisjunctionMatchers
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import scalaz.{-\/, \/, \/-}
import sts.libs.errors._


class JanitorSpec extends Specification with DisjunctionMatchers {

    "JsonFunction" should {

        val badJson = """{"batchId":2,"orderBy":"name","thing":{"id":1,"name":"dog","rank":1},"types":["mosque","park","casino"}"""

        "Catch an exception when parsing malformed json" in {

            val result: \/[GenError, JsValue] = JsonFunction.parse(badJson)

            result must be_-\/

            val error = result.swap.toOption.collect {
                case e: JsonParseError => e
            }

            error must beSome.which { err =>
                err.source must be_-\/(badJson)
            }
        }

        val testJsObj = Json.obj("id" -> 1, "name" -> "pizza", "rank" -> 3.2)

        tag("path")
        "Map a JsError to a JsonMissingPath error" in {

            val testReads = (__ \ "emu").read[Long]

            val result: \/[GenError, Long] = JsonFunction.read(testJsObj)(testReads)

            result must be_-\/

            val error = result.swap.toOption.collect {
                case e: JsonMissingPath => e
            }

            error must beSome.which { err =>
                err.source must be_\/-(testJsObj)
            }
        }

        "Map a JsError to a JsonUnexpectedType error" in {

            val testReads = (__ \ "rank").read[Int]

            val result: \/[GenError, Int] = JsonFunction.read(testJsObj)(testReads)

            result must be_-\/

            val error = result.swap.toOption.collect {
                case e: JsonUnexpectedType => e
            }

            error must beSome.which { err =>
                err.source must be_\/-(testJsObj)
            }
        }
    }
}