package sts.diplomat.models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class ApiKey(
    id: Long,
    name: String,
    scope: String,
    key: Option[String]
)

object ApiKey {


    def empty: ApiKey = ApiKey(0L, "", "", None)

    val parser: RowParser[ApiKey] = {
        get[Long]("api_keys.id") ~
        get[String]("api_keys.name") ~
        get[String]("api_keys.scope") ~
        get[Option[String]]("api_keys.key")
    } map {
        case id~name~scope~key => ApiKey(id, name, scope, key)
    }
}