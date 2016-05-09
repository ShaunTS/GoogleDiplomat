package sts.util.db

import java.sql.Connection
import play.api.db.Database
import scalaz.{-\/, \/, \/-}


/**
 * The classes in this file can be used to run SQL scripts (written in evolutions
 * format with !Ups and !Downs sections) before and after a given unit test. The
 * purpose being to populate a test database with sample data, for each individual
 * unit test to interact with.
 */

class EvoScriptRunner(val sqlFiles: Seq[String], db: Database) {

    lazy val evoFiles: Seq[EvoScript] = sqlFiles.map(EvoScript(_))

    def down: \/[Throwable, Boolean] = db.withConnection { implicit c =>
        evoFiles.reverse.map(_.applyDowns).collectFirst {
            case e @ -\/(_) => e
        }.getOrElse(\/-(true))
    }

    def up: \/[Throwable, Boolean] = db.withConnection { implicit c =>
        evoFiles.map(_.applyUps).collectFirst {
            case e @ -\/(_) => e
        }.getOrElse(\/-(true))
    }

    def uncatch(result: \/[Throwable, Boolean]): Unit = result.swap.foreach(throw _)

    def unsafeUp() = uncatch(up)

    def unsafeDown() = uncatch(down)

}

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

    def applyScript(lines: List[String])(implicit c: Connection): \/[Throwable, Boolean] =
        lines.map(execute).collectFirst {
            case e @ -\/(_) => e
        }.getOrElse(\/-(true))

    def applyUps(implicit c: Connection): \/[Throwable, Boolean] =
        applyScript(this.ups)(c)

    def applyDowns(implicit c: Connection): \/[Throwable, Boolean] =
        applyScript(this.downs)(c)

    def execute(sql: String)(implicit c: Connection): \/[Throwable, Boolean] =
        \/.fromTryCatchThrowable[Boolean, Throwable] {
            c.createStatement.execute(sql)
        }
}

object EvoScript {

    def apply(path: String): EvoScript = {

        val content = io.Source.fromFile(path).mkString

        EvoScript(path, content, Ups(content), Downs(content))
    }
}