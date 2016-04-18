package sts.diplomat.models

import play.api.libs.json.{__, Reads, Json}
import play.api.libs.functional.syntax._

case class Place(
    geometry: Geometry,
    name: String,
    placeId: String
)