package sts.libs.db

import java.sql.{Connection, SQLException}
import sts.libs.errors._
import scalaz.{-\/, \/, \/-}
import scala.util.{Try}
import org.postgresql.util.PSQLException
import play.api.db.Database

trait QuietSQL {

    def sqlErrorMap(e: Throwable)(implicit c: Connection): GenError = e match {
        case psql: PSQLException if(psql.getSQLState equals "23505") => UniqueKeyViolation.fromDB(psql)(c)
        case psql: PSQLException => MiscPSQLError(psql)
        case sql: SQLException => MiscSQLError(sql)
        case misc => MiscThrown(misc)
    }

    def apply[T](op: =>T)(implicit c: Connection): \/[GenError, T] =
        \/.fromTryCatchThrowable[T, Throwable](op)
            .leftMap(sqlErrorMap(_))
}

class PSQLHandler(db: Database) extends QuietSQL {
    
}