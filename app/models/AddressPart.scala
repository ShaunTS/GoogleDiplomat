package sts.diplomat.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads, Json}

case class AddressPart(
    longName: String,
    shortName: String,
    types: List[String]
)

object AddressPart {

    implicit val reads: Reads[AddressPart] = (
        (__ \ "long_name").read[String] and
        (__ \ "short_name").read[String] and
        (__ \ "types").read[List[String]].orElse(Reads.pure(Nil))
    )(AddressPart.apply _)
}