package sts.libs.db

import java.sql.Connection
import play.api.db.Database
import scala.util.{Failure, Success, Try}
import scalaz.{-\/, \/, \/-}
import sts.libs.errors._
import sts.libs.{GenericHandler, GenErrorMap}

/**
 *  @todo Add comments
 */


class PSQLFunctions(
    val schema: SchemaInfo,
    val errorMap: PSQLErrorMap
) extends GenericHandler[PSQLErrorMap] {



    def transact[A](task: Connection => \/[GenError, A])(implicit db: Database): \/[GenError, A] =
        db.withConnection(autocommit = false) { implicit connection =>
            val result = task(connection)

            result fold (
                error => connection.rollback(),
                success => connection.commit()
            )

            result
        }

    def withConnection[A](task: Connection => A)(implicit db: Database): \/[GenError, A] =
        db.withConnection { implicit c => apply(task(c)) }

    def optional[A](task: Connection => Option[A], noResult: SQLNoResult)(implicit db: Database): \/[GenError, A] =
        db.withConnection { implicit c => optional(task(c), noResult.withURL(db.url) ) }




    def withTransaction[A](task: Connection => A)(implicit db: Database): \/[GenError, A] =
        transact(implicit c => apply(task(c)))

    def transactOption[A](task: Connection => Option[A], noResult: SQLNoResult)(implicit db: Database): \/[GenError, A] =
        transact(implicit c => optional(task(c), noResult))


    def find[A](task: Connection => Option[A])(implicit db: Database): \/[GenError, A] = optional(task, FailedRead)

    def create[A](task: Connection => Option[A])(implicit db: Database): \/[GenError, A] = optional(task, FailedCreate)

    def update[A](task: Connection => Option[A])(implicit db: Database): \/[GenError, A] = optional(task, FailedUpdate)




    def apply[A](task: => A)(implicit c: Connection): \/[GenError, A] =
    Try { task } match {
        case Success(s) => \/-(s)
        case Failure(e) => -\/(errorMap(e))
    }

    def optional[A](task: => Option[A], noResult: SQLNoResult)(implicit c: Connection): \/[GenError, A] =
    apply(task)(c) flatMap {
        case Some(result) => \/-(result)
        case None => -\/( noResult.withURL(c.getMetaData.getURL) )
    }

    def create[A](task: => Option[A])(implicit c: Connection): \/[GenError, A] = optional(task, FailedCreate)

    def update[A](task: => Option[A])(implicit c: Connection): \/[GenError, A] = optional(task, FailedUpdate)



    def FailedAction: SQLNoResult = (schema makeError "Failed action from")

    def FailedRead: SQLNoResult = (schema makeError "Could not find requested")

    def FailedCreate: SQLNoResult = (schema makeError "Failed to create")

    def FailedUpdate: SQLNoResult = (schema makeError "Failed to update")
}


trait PostgresOps {

    def errorMap: PSQLErrorMap = new PSQLErrorMap()

    def schema: SchemaInfo

    lazy val PSQLHandler: PSQLFunctions = new PSQLFunctions(schema, errorMap)
}


class PSQLErrorMap extends GenErrorMap {

    val errors = List(
        UniqueKeyViolation.diagnose,
        MiscPSQLError.diagnose,
        MiscSQLError.diagnose,
        nomatch
    )

}

case class SchemaInfo(tableName: String, entityName: String) {


    def makeError(action: String) = SQLNoResult(
        table = Some(tableName),
        dbURL = None,
        message = s"$action `$entityName` in relation `$tableName`"
    )
}
