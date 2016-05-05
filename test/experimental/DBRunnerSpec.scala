package test.unit.util

import sts.diplomat.models._
import sts.util.TimeKeeping
import sts.util.dbrunner._
import sts.util.debug.helpers._

import anorm.{SQL, SqlParser, NamedParameter, ParameterValue}
import org.specs2.mutable._
import play.api.db.{Databases, PooledDatabase, DatabaseConfig}
import play.api.db.evolutions._
import play.api.Configuration
import play.api.test._
import play.api.test.Helpers._

import play.api.inject.guice.GuiceApplicationLoader
import play.api.{ Application, ApplicationLoader, Environment, Mode }

import play.api.db.Database


object DBRunnerSpec extends Specification with TimeKeeping {

    def mkTestDB = Databases(
        driver = "org.postgresql.Driver",
        url = "jdbc:postgresql://localhost/google_diplomat_test",
        config = Map(
            "user" -> "local_app",
            "password" -> "stsapp"
        )
    )

    "DB stuff" >> {

        "app" in {

            val config = Configuration.load(Environment.simple())
            val keys0 = config.getConfig("db.test")
                .get.keys.toSeq.sorted

            val keys1 = Configuration.reference.getConfig("play.db.prototype")
                .get.keys.toSeq.sorted
            
            
            
            pl("configs")(keys0.mkString("\n") +"\n\n"+ keys1.mkString("\n"))
            
            success
        }
    }

        // idleMaxAge: "1 minutes",
        // idleConnectionTestPeriod: "20 seconds",
        // connectionTimeout: "20 seconds",
        // connectionTestStatement: "SELECT 1",
        // maxConnectionAge: "30 minutes",
        // bonecp.logStatements: true,
        // pool: "bonecp"

    def insertEmu(db: Database): Option[Long] = db.withConnection { implicit c =>
        SQL("""
            INSERT INTO fake_users(name) VALUES
            ({name})
        """).on(
            "name" -> "emu"
        ).executeInsert()
    }
}