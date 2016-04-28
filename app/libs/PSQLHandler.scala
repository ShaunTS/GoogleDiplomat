package sts.libs.db

import java.sql.{Connection, SQLException}
import sts.libs.errors._
import scalaz.{-\/, \/, \/-}
import scala.util.{Failure, Success, Try}
import org.postgresql.util.PSQLException
import play.api.db.Database
import sts.libs.{GenericHandler, GenErrorMap}

class PSQLErrorMap extends GenErrorMap {

    val errors = List(
        UniqueKeyViolation.diagnose,
        MiscPSQLError.diagnose,
        MiscSQLError.diagnose,
        nomatch
    )

}

class PSQLFunctions(val errorMap: PSQLErrorMap) extends GenericHandler[PSQLErrorMap] {

    def apply[T](block: => T)(implicit c: Connection): \/[GenError, T] =
        Try { block } match {
            case Success(s) => \/-(s)
            case Failure(e) => -\/(errorMap(e))
        }
}