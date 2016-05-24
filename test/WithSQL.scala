package sts.test

import org.specs2.execute.{ AsResult, Result }
import org.specs2.mutable.Around
import org.specs2.specification.Scope
import play.api.Application
import play.api.db.Database
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._
import sts.libs.diplomat.TestComponents
import sts.util.db.EvoScriptRunner

abstract class WithSQL(val app: Application, val sqlFiles: String *) extends Around with Scope {

    def this(sqlFiles: String *) = this(GuiceApplicationBuilder().build(), sqlFiles: _ *)

    implicit def implicitApp = app

    implicit def implicitMaterializer = app.materializer

    lazy implicit val testDB: Database = app.injector.instanceOf[TestComponents].db

    lazy val data = new EvoScriptRunner(sqlFiles, testDB)

    override def around[T: AsResult](test: => T): Result = Helpers.running(app) {

        data.down
        data.unsafeUp()

        try { eval(test) } finally { testDB.shutdown() }
    }

    def eval[T: AsResult](block: => T): Result = AsResult.effectively {
        try {
            block
        }
        finally {
            data.unsafeDown()
        }
    }

}
