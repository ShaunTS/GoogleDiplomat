package sts.util.dbrunner

import scala.io.{BufferedSource}
import java.sql.Connection
import play.api.db.Database
import scalaz.{-\/, \/, \/-}


/** CONTENTS (WORK IN PROGRESS)
 *
 * When finished, the classes contained in this file will be used to run SQL scripts
 * (written in evolutions format with !Ups and !Downs sections) before and after a
 * given unit test. The purpose being to populate a test database with sample data,
 * for each individual unit test to interact with.
 */
trait EvoScriptOps {

    val upsTag = "# --- !Ups"
    val downsTag = "# --- !Downs"

    def cleanSplit(arg: String, tag: String): List[String] =
        arg.split(tag).map(_.trim).filterNot(_.isEmpty).toList

    def collectUps(page: String): List[String] = {

        val upStart = Some(page.indexOfSlice(upsTag)).filterNot(_ < 0)
            .map(_ + upsTag.length)

        val upEnd = Some(page.indexOfSlice(downsTag)).filterNot(_ < 0)
            .getOrElse(page.length)


        upStart.map(page.slice(_, upEnd).trim).filterNot(_.isEmpty).toList
            .flatMap(cleanSplit(_, ";").toList)
    }

    def collectDowns(page: String): List[String] =
        Some(page.indexOfSlice(downsTag)).filterNot(_ < 0)
            .map(_ + downsTag.length)
            .map(page.slice(_, page.length).trim)
            .filterNot(_.isEmpty).toList
            .flatMap(cleanSplit(_, ";").toList)

}

case class Ups(lines: List[String])

object Ups extends EvoScriptOps {

    def apply(page: String): Ups = Ups(collectUps(page))

}

case class Downs(lines: List[String])

object Downs extends EvoScriptOps {

    def apply(page: String): Downs = Downs(collectDowns(page))

}

case class EvoScript(
    path: String,
    page: String,
    upScript: Ups,
    downScript: Downs
) {

    def ups: List[String] = this.upScript.lines

    def downs: List[String] = this.downScript.lines

    def applyUps(db: Database): List[Boolean] = {
        implicit val conn = db.getConnection(autocommit = true)

        val result = this.ups.map(execute)

        conn.close()
        result
    }

    def applyDowns(db: Database): List[Boolean] = {

        implicit val conn = db.getConnection(autocommit = true)

        val result = this.downs.map(execute)

        conn.close()
        result
    }

    def execute(sql: String)(implicit c: Connection) = {
        c.createStatement.execute(sql)
    }
}

object EvoScript {

    def apply(path: String): EvoScript = {

        val content = io.Source.fromFile(path).mkString

        EvoScript(path, content, Ups(content), Downs(content))
    }
}


// val files = List(
//     "test/snapshots/test_sql_file.sql",
//     "test/snapshots/just_downsA.sql",
//     "test/snapshots/just_downsB.sql",
//     "test/snapshots/just_upsA.sql",
//     "test/snapshots/just_upsB.sql"
// ).map(io.Source.fromFile(_).mkString)