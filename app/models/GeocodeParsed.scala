package sts.diplomat.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads, Json}

case class GeocodeParsed(
    placeId: Option[String],
    formattedAddr: Option[String],
    locationType: String,
    parts: List[AddressPart],
    geometry: Geometry,
    placeTypes: List[String]
)

object GeocodeParsed {

    implicit val reads: Reads[GeocodeParsed] = (
        (__ \ "place_id").readNullable[String] and
        (__ \ "formatted_address").readNullable[String] and
        (__ \ "location_type").read[String] and
        (__ \ "address_components").read[List[AddressPart]].orElse(Reads.pure(Nil)) and
        (__ \ "geometry").read[Geometry] and
        (__ \ "types").read[List[String]].orElse(Reads.pure(Nil))
    )(GeocodeParsed.apply _)
}

