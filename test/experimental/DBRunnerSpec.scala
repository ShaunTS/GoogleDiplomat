package test.unit.util

import sts.diplomat.models._
import sts.util.TimeKeeping
import sts.util.dbrunner._
import sts.util.debug.helpers._

import anorm.{SQL, SqlParser}
import org.specs2.mutable._
import play.api.db.Databases
import play.api.db.evolutions._
import play.api.Environment
import play.api.Play
import play.api.test._
import play.api.test.Helpers._

import scala.util.{Try, Success, Failure}
import java.sql.{Connection, DatabaseMetaData, SQLException}
import org.postgresql.util.{PSQLException, PSQLState, ServerErrorMessage}


object DBRunnerSpec extends Specification with TimeKeeping {

    "DB stuff" >> {

        tag("env")
        "env config" in {
            pl("env")(Environment.simple())

            success
        }

        tag("dberr")
        "DBApi" in {

            val db = Databases(
                driver = "org.postgresql.Driver",
                url = "jdbc:postgresql://localhost/google_diplomat_test",
                config = Map(
                    "user" -> "local_app",
                    "password" -> "stsapp"
                )
            )

            db.withConnection { implicit c =>

                printMeta

                Try {
                    val query = SQL("""
                        INSERT INTO fake_users_groups(user_id, group_id) VALUES ({user_id}, {group_id})
                    """).on(
                        "user_id" -> 1,
                        "group_id" -> 1
                    )
                    .executeInsert(SqlParser.scalar[Long].singleOpt)

                    // pl("query")(query)
                } match {
                    case Success(s) => ()
                    case Failure(e: PSQLException) => printExc(e)
                    case Failure(e) => pl("ERR")(List(e, e.getMessage).mkString("\n"))
                }

            }

            db.shutdown()
            success
        }
    }



    def printMeta(implicit c: Connection) {
        val info = c.getMetaData
        println("db-url = " +info.getURL)
    }

    def printServerMsg(msg: ServerErrorMessage) {

        List(
            "getDetail",
            "getHint",
            "getTable",
            "getConstraint",
            "getMessage",
            "getSchema",
            "getFile",
            "getWhere",
            "getColumn",
            "getInternalQuery",
            "getSeverity",
            "getRoutine",
            "toString"
        ).zip(List(
            msg.getDetail,
            msg.getHint,
            msg.getTable,
            msg.getConstraint,
            msg.getMessage,
            msg.getSchema,
            msg.getFile,
            msg.getWhere,
            msg.getColumn,
            msg.getInternalQuery,
            msg.getSeverity,
            msg.getRoutine,
            msg.toString)
        ).map {
                case(tag, thing) => "\t" + tag + ": " + thing
            }.foreach(println)
    }

    def printExc(e: PSQLException) = {
        println("\n------------------------------------------------------------")

        List("e", "msg", "state", "code", "next").map("\t" + _)
            .zip(List(e, e.getMessage, e.getSQLState, e.getErrorCode, e.getNextException))
            .map {
                case(tag, thing) => "\t" + tag + ": " + thing + "\n"
            }.foreach(println)
        println("------------------------------------------------------------")
        printServerMsg(e.getServerErrorMessage)
        println("------------------------------------------------------------\n")
    }

}