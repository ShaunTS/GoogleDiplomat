package sts.test

import javax.inject.Inject
import org.specs2.execute.{ AsResult, Result }
import org.specs2.mutable.Around
import org.specs2.specification.Scope
import play.api.Application
import play.api.db.{ Database, NamedDatabase }
import play.api.test._
import sts.libs.diplomat.TestComponents
import sts.util.db.EvoScriptRunner
import sts.util.debug.psqlHelpers._

abstract class WithSQL(val app: Application, val sqlFiles: String *) extends Around with Scope {

    def this(sqlFiles: String *) = this(FakeApplication(), sqlFiles: _ *)

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
