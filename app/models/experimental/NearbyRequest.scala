package sts.diplomat.models

import anorm._
import anorm.SqlParser._
import java.sql.Connection
import play.api.db.DB
import play.api.libs.json.{Json, Writes, Reads}
import play.api.Play.current
import scala.util.{Try, Success, Failure}
import scalaz.{\/}
import sts.libs.json.{JsParam, JsParamList, JsParamLists}



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

object NearbyRequest {

    val baseUrl: String = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"

    // implicit val writes: Writes[NearbyRequest] = (
    //     (__ \ "id").writeNullable[Long] and
    //     (__ \ "loc").write[Coords] and
    //     (__ \ "radius").write[Int] and
    //     (__ \ "pageToken").writeNullable[String]
    // )

    def empty: NearbyRequest = NearbyRequest(None, Coords.empty, 0, None, NearbyParams.default)

    def create(req: NearbyRequest): Option[NearbyRequest] = {

        DB.withTransaction { implicit c =>

            SQL("""
                INSERT INTO nearby_search_requests(lat, lng, radius, params) VALUES
                ({lat}, {lng}, {radius}, jsonOrNull({params}))
            """).on(
                "lat" -> req.loc.lat,
                "lng" -> req.loc.lng,
                "radius" -> req.radius,
                "params" -> req.extraParams.stringOpt
            ).executeInsert()
                .map(req.withId(_))
        }
    }

}