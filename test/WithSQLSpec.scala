package test.unit.util

import anorm.SqlParser._
import anorm.{SQL, SqlParser, NamedParameter, ParameterValue}
import java.text.SimpleDateFormat
import java.util.Date
import org.specs2.mutable._
import play.api.db.{Database, NamedDatabase}
import play.api.test._
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import sts.test._
import sts.test.helpers._
import test.mock.models.{Group, User}

class WithSQLSpec extends PlaySpecification {

    val sqlFile = "test/snapshots/evo-script/testEvo_1.sql"

    val sqlFiles = Seq(
        "test/snapshots/evo-script/testEvo_1.sql",
        "test/snapshots/evo-script/testEvo_2.sql"
    )

    "WithSQL database runner" should {

        "Load an sql script and perform tests, then undo the script" in new WithSQL(sqlFile) {

            rowCount("fake_users") must equalTo(2L)
            rowCount("fake_groups") must equalTo(2L)

            val user1: Option[User] = User.read(1L)

            user1 must beSome.which { user =>
                user.id must beSome(1L)
                user.name must equalTo("Jawa-01")
                user.email must equalTo("joe01@gmail.jawa")

                VerifyDate(user.created) hasTimestamp("2016-05-08 16:01:10.642-04")
            }
        }


        "Load multiple sql scripts in order, then undo them in reverse order" in new WithSQL(sqlFiles: _ *) {

            rowCount("fake_users_groups") must equalTo(3L)
        }
    }

    def rowCount(tableName: String)(implicit db: Database): Long =
        db.withConnection { implicit c =>
            SQL(s"SELECT COUNT(*) FROM $tableName").as(scalar[Long].single)
        }

}