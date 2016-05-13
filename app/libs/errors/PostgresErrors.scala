package sts.libs.errors

import java.lang.Throwable
import java.sql.{Connection, SQLException}
import org.postgresql.util.PSQLException
import scalaz.{-\/, \/, \/-}
import sts.libs.functional.{FunctionFragment, PiecewiseFunction}



trait SQLError[E] extends GenError {
    def dbURL: Option[String]
    def table: Option[String]

    def withURL(url: String): E
}

case class SQLNoResult(
    message: String,
    table: Option[String],
    dbURL: Option[String]
) extends SQLError[SQLNoResult] {

    val cause = None

    def withURL(url: String) = this.copy(dbURL = Some(url))
}

case class MiscSQLError(
    message: String,
    sqlError: SQLException,
    sqlState: String,
    dbURL: Option[String] = None,
    table: Option[String] = None
) extends SQLError[MiscSQLError] {

    def cause = Some(this.sqlError)

    def withURL(url: String) = this.copy(dbURL = Some(url))
}

object MiscSQLError extends Diagnosed {

    def diagnose = PartialFunction[Throwable, GenError] {
        case e: SQLException => apply(e)
    }

    def apply(e: SQLException): MiscSQLError = MiscSQLError(e.getMessage, e, e.getSQLState)
}




trait PostgresError[E] extends SQLError[E] {

    def detail: String
}


case class MiscPSQLError(
    message: String,
    sqlError: PSQLException,
    sqlState: String,
    dbURL: Option[String] = None,
    table: Option[String] = None,
    detail: String = ""
) extends PostgresError[MiscPSQLError] {

    def cause = Some(this.sqlError)

    def withURL(url: String) = this.copy(dbURL = Some(url))

}
object MiscPSQLError extends Diagnosed {

    def diagnose = PartialFunction[Throwable, GenError] {
        case e: PSQLException => apply(e)
    }

    def apply(e: PSQLException): MiscPSQLError = MiscPSQLError(e.getMessage, e, e.getSQLState)
}



private[errors] trait FailedConstraint[E] extends PostgresError[E] {
    def constraint: String
}

private[errors] trait ForeignKeyViolation[E] extends FailedConstraint[E] {

    def key: String
    def foreignTable: String
    def foreignKey: String
}


case class UniqueKeyViolation(
    message: String,
    constraint: String,
    conflicts: List[(String, String)],
    dbURL: Option[String] = None,
    table: Option[String] = None,
    detail: String = "",
    cause: Option[Throwable] = None
) extends FailedConstraint[UniqueKeyViolation] {

    def withURL(url: String) = this.copy(dbURL = Some(url))
}

object UniqueKeyViolation extends Diagnosed {

    def diagnose = PartialFunction[Throwable, GenError] {
        case psql: PSQLException if(psql.getSQLState equals "23505") => UniqueKeyViolation(psql)
    }

    val regex = """Key \((.*)\)=\((.*)\) already exists.""".r

    def apply(e: PSQLException): UniqueKeyViolation = {
        val msg = e.getServerErrorMessage
        val regex(columns, values) = msg.getDetail

        val conflicting =
            (columns.split(", ").toList zip values.split(", ").toList)

        new UniqueKeyViolation(
            msg.getMessage, msg.getConstraint, conflicting, None, Some(msg.getTable), msg.getDetail, None
        )
    }

    def fromDB(e: PSQLException)(implicit c: Connection): UniqueKeyViolation = {
        val url = c.getMetaData.getURL
        val msg = e.getServerErrorMessage
        val regex(columns, values) = msg.getDetail

        val conflicting =
            (columns.split(", ").toList zip values.split(", ").toList)

        new UniqueKeyViolation(
            msg.getMessage, msg.getConstraint, conflicting, Some(url), Some(msg.getTable), msg.getDetail, None
        )
    }

    def empty: UniqueKeyViolation = new UniqueKeyViolation("", "", Nil, None, None, "", None)
}