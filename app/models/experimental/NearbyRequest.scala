package sts.diplomat.models

import anorm._
import anorm.SqlParser._
import org.postgresql.util.PGobject
import play.api.db.Database
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Json, Writes, Reads}
import scalaz.{\/}
import sts.libs.db.PostgresHelpers._
import sts.libs.db.{PostgresOps, SchemaInfo}
import sts.libs.errors.GenError
import sts.libs.json.{JsParam, JsParamList, JsParamLists}

case class NearbyRequest(
    id: Option[Long],
    loc: Coords,
    radius: Int,
    pageToken: Option[String] = None,
    extraParams: NearbyParams = NearbyParams.default
) extends Model[NearbyRequest] {

    def withId(id: Long): NearbyRequest = this.copy(id = Some(id))

    def withParams(params: JsParam[_] * ): NearbyRequest = this.copy(
        extraParams = this.extraParams.withValues(params: _ *)
    )
}

object NearbyRequest extends PostgresOps {

    val schema = SchemaInfo(
        tableName = "nearby_search_requests",
        entityName = "NearbyRequest"
    )

    val baseUrl: String = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"

    def empty: NearbyRequest = NearbyRequest(None, Coords.empty, 0, None, NearbyParams.default)

    val parser: RowParser[NearbyRequest] = {
        get[Option[Long]]("nearby_search_requests.id") ~
        Coords.parser("nearby_search_requests") ~
        int("nearby_search_requests.radius") ~
        get[Option[String]]("nearby_search_requests.pg_token") ~
        get[Option[PGobject]]("nearby_search_requests.params").map(_.map(_.getValue)) map {
            case id~coords~radius~token~paramsJSON =>
                NearbyRequest(id, coords, radius, token, NearbyParams.parse(paramsJSON))
        }
    }

    def create(req: NearbyRequest)(implicit db: Database): \/[GenError, NearbyRequest] = PSQLHandler.create { implicit c =>

        SQL("""
            INSERT INTO nearby_search_requests(lat, lng, radius, pg_token, params) VALUES
            ({lat}, {lng}, {radius}, {token}, toJSON({params}))
        """).on(
            "lat" -> req.loc.lat,
            "lng" -> req.loc.lng,
            "radius" -> req.radius,
            "token" -> req.pageToken,
            "params" -> req.extraParams.stringOpt
        ).executeInsert()
            .map(req.withId(_))
    }

    def find(id: Long)(implicit db: Database): \/[GenError, NearbyRequest] = PSQLHandler.find { implicit c =>
        SQL("SELECT * FROM nearby_search_requests WHERE id = {id}").on("id" -> id)
        .as(parser.singleOpt)
    }

    implicit val writes: Writes[NearbyRequest] = Json.writes[NearbyRequest]

    implicit val reads: Reads[NearbyRequest] = (
        (__ \ "id").readNullable[Long] and
        (__ \ "loc").read[Coords] and
        (__ \ "radius").read[Int] and
        (__ \ "pageToken").readNullable[String] and
        (__ \ "extraParams").read[NearbyParams]
    )(NearbyRequest.apply _)
}


case class NearbyParams(params: Seq[JsParam[_]]) extends JsParamList[NearbyParams] {

    def withValues(p: JsParam[_] *): NearbyParams = this.copy(params = this.mergeValues(p))
}

object NearbyParams extends JsParamLists[NearbyParams] {

    def defaultKeys = Seq(
        JsParam.empty[String]("rankby"),
        JsParam.empty[String]("keyword"),
        JsParam.empty[Int]("minprice"),
        JsParam.empty[Int]("maxprice"),
        JsParam.empty[String]("name"),
        JsParam.empty[Boolean]("opennow"),
        JsParam.empty[String]("type"),
        JsParam.empty[List[String]]("types")
    )
}