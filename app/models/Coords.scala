package sts.diplomat.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads, Writes, Json}
import anorm.{ ~, RowParser }
import anorm.SqlParser._

/** CONTENTS
 *
 *  Classes used to represents Geocoordinate value pairs, specifically for the purose
 *  of being read from or written to the json format used by Google Places API.
 */


case class Coords(lat: Double, lng: Double) {

    def mkString: String = List(this.lat, this.lng).mkString(",")
}

object Coords {

    def parser(tableName: String): RowParser[Coords] = {
        double(s"$tableName.lat") ~ double(s"$tableName.lng") map {
            case lat~lng => Coords(lat, lng)
        }
    }

    implicit val writes: Writes[Coords] = Json.writes[Coords]

    implicit val reads: Reads[Coords] = (
        (__ \ "lat").read[Double] and
        (__ \ "lng").read[Double]
    )(Coords.apply _)

    def empty: Coords = Coords(0D, 0D)
}

case class GeoBounds(northeast: Coords, southwest: Coords) {

    def ne: Coords = this.northeast

    def sw: Coords = this.southwest
}
object GeoBounds {

    implicit val reads: Reads[GeoBounds] = (
        (__ \ "northeast").read[Coords] and
        (__ \ "southwest").read[Coords]
    )(GeoBounds.apply _)

    def empty: GeoBounds = GeoBounds(Coords.empty, Coords.empty)
}


case class Geometry(
    loc: Coords,
    viewport: GeoBounds,
    bounds: Option[GeoBounds]
)
object Geometry {

    implicit val reads: Reads[Geometry] = (
        (__ \ "location").read[Coords] and
        (__ \ "viewport").read[GeoBounds] and
        (__ \ "bounds").readNullable[GeoBounds]
    )(Geometry.apply _)

    def empty: Geometry = Geometry(Coords.empty, GeoBounds.empty, None)
}