package test.mock.models

import anorm._
import anorm.SqlParser._
import java.sql.Connection
import play.api.db.{Database, DB}
import play.api.libs.json.{Json, Writes, Reads}
import scala.util.{Try, Success, Failure}
import scalaz.{\/}
import sts.libs.json.{JsParam, JsParamList, JsParamLists}
import sts.diplomat.models.Model
import java.util.Date
import play.api.Play.current

case class Group(id: Option[Long], name: String) extends Model[Group] {

    def withId(id: Long): Group = this.copy(id = Some(id))
}
object Group {

    def empty: Group = Group(None, "")

    val parser: RowParser[Group] = {
        get[Option[Long]]("fake_groups.id") ~
        get[String]("fake_groups.name") map {
            case id~name => Group(id, name)
        }
    }

    def create(group: Group)(implicit db: Database): Option[Group] =
        db.withTransaction { implicit c =>

            SQL("""
                INSERT INTO fake_groups(name) VALUES ({name})
            """).on(
                "name" -> group.name
            ).executeInsert().map(group.withId)
        }

    def read(id: Long)(implicit db: Database): Option[Group] = db.withConnection { implicit c =>
        SQL("""
            SELECT * FROM fake_groups
            WHERE id = {id}
        """).on(
            "id" -> id
        ).as(parser.singleOpt)
    }
}


case class User(
    id: Option[Long],
    name: String,
    email: String,
    created: Date,
    groups: List[Group] = Nil,
    notes: List[String] = Nil
) extends Model[User] {

    def withId(id: Long): User = this.copy(id = Some(id))
}
object User {

    def empty: User = User(None, "", "", new Date)

    val simple: RowParser[User] = {
        get[Option[Long]]("fake_users.id") ~
        get[String]("fake_users.name") ~
        get[String]("fake_users.email") ~
        get[Date]("fake_users.created") map {
            case id~name~email~created => User(id, name, email, created)
        }
    }

    def read(id: Long)(implicit db: Database): Option[User] = db.withConnection { implicit c =>

        SQL("""
            SELECT * FROM fake_users
            WHERE id = {id}
        """).on(
            "id" -> id
        ).as(simple.singleOpt)
    }


}

