package test.unit.libs


import play.api.libs.json._
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.fasterxml.jackson.core.{JsonLocation, JsonProcessingException}
import sts.libs.errors._
import scalaz.{-\/, \/, \/-}
import org.postgresql.util.{PSQLException, PSQLState, ServerErrorMessage}
import org.specs2.matcher.DisjunctionMatchers

class ErrorSpec extends Specification with DisjunctionMatchers {


    "Custom PSQL Errors" should {

t
        "Construct a one-column UniqueKeyViolation" in {
            val error = MockPSQLErrors.oneColumnUniqueViolation
            val custom = UniqueKeyViolation(error)

            custom.table must beSome("fake_users")
            custom.constraint must equalTo("fake_users_pkey")
            custom.detail must equalTo("Key (id)=(1) already exists.")

            custom.conflicts must contain(
                exactly("id" -> "1")
            )
        }
br
        "Construct a two-column UniqueKeyViolation" in {
            val error = MockPSQLErrors.twoColumnUniqueViolation
            val custom = UniqueKeyViolation(error)

            custom.table must beSome("fake_users_groups")
            custom.constraint must equalTo("fake_users_groups_user_id_group_id_key")
            custom.detail must equalTo("Key (user_id, group_id)=(1, 1) already exists.")

            custom.conflicts must contain(
                exactly("user_id" -> "1", "group_id" -> "1")
            )
        }
    }

br
br
    section("json-errors")
    "Custom Json Errors" should {

        "Construct a JsonParseError from a Jackson exception" in {
            val error = MockJsonErrors.parseException
            val custom = JsonParseError(error)

            custom.message must be equalTo(
                """Unexpected end-of-input: expected close marker for OBJECT (from [Source: {"id":3,"name":"pizza","rank":4; line: 1, column: 0])"""
            )

            custom.source must be_-\/("""{"id":3,"name":"pizza","rank":4""")

            custom.line must be_==(1)
            custom.colA must beSome(0)
            custom.colB must beSome(94)
        }

br
        "Construct a JsonMissingPath from a Play JsError" in {
            val error = MockJsonErrors.missingPathError
            val custom = JsonMissingPath(error)

            val expectedPath: PathNode = KeyPathNode("emu")

            custom.path.path.headOption must beSome(expectedPath)
        }

br
        "Construct a JsonUnexpectedType from a Play JsError" in {
            val error = MockJsonErrors.wrongTypeError
            val custom = JsonUnexpectedType(error)

            val expectedPath: PathNode = KeyPathNode("rank")
            custom.path.path.headOption must beSome(expectedPath)
        }
    }
    section("json-errors")

}

object MockJsonErrors {

    def parseException = new JsonProcessingException("") {

        override def getLocation = new JsonLocation("""{"id":3,"name":"pizza","rank":4""", 0, 1, 94)

        override def getMessage = """Unexpected end-of-input: expected close marker for OBJECT (from [Source: {"id":3,"name":"pizza","rank":4; line: 1, column: 0])
at [Source: {"id":3,"name":"pizza","rank":4; line: 1, column: 94]"""

    }

    def wrongTypeError = (__ \ "rank").read[Long].reads {
        Json.obj(
            "id" -> 1,
            "name" -> "dog",
            "rank" -> 3.2
        )
    } match {
        case e: JsError => e
        case _ => new JsError(Nil)
    }

    def missingPathError = (__ \ "emu").read[Long].reads {
        Json.obj(
            "id" -> 1,
            "name" -> "dog",
            "rank" -> 3.2
        )
    } match {
        case e: JsError => e
        case _ => new JsError(Nil)
    }
}

object MockPSQLErrors {

    def miscError = new PSQLException("Some random error", new PSQLState("12345"))

    def oneColumnUniqueViolation = new PSQLException(oneColumnUnique)

    def twoColumnUniqueViolation = new PSQLException(twoColumnUnique)

    def oneColumnUnique: ServerErrorMessage = new ServerErrorMessage("", 0) {

        override def getSQLState = "23505"
        override def getDetail = "Key (id)=(1) already exists."
        override def getMessage = """duplicate key value violates unique constraint "fake_users_pkey""""
        override def getTable = "fake_users"
        override def getConstraint = "fake_users_pkey"

        override def toString =
"""ERROR: duplicate key value violates unique constraint "fake_users_pkey"
Detail: Key (id)=(1) already exists."""
    }

    def twoColumnUnique: ServerErrorMessage = new ServerErrorMessage("", 0) {

        override def getSQLState = "23505"
        override def getDetail = "Key (user_id, group_id)=(1, 1) already exists."
        override def getMessage = """duplicate key value violates unique constraint "fake_users_groups_user_id_group_id_key""""
        override def getTable = "fake_users_groups"
        override def getConstraint = "fake_users_groups_user_id_group_id_key"

        override def toString =
"""ERROR: duplicate key value violates unique constraint "fake_users_groups_user_id_group_id_key"
Detail: Key (user_id, group_id)=(1, 1) already exists."""
    }
}