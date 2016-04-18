package test.unit

import sts.diplomat.models._

import org.specs2.mutable._

// import play.api.Play.current
import play.api.test._
import play.api.test.Helpers._
import play.api.Play
import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import play.api.libs.json.Json


object WSSpec extends Specification {

    "Play WS" should {

        val loc = Coords(41.4804518, -73.2200572)

        // "Read Json" in new WithApplication {

        //     // println(Json.toJson(loc))

        //     val jGeo = """{"location":{"lat":41.48607489999999,"lng":-73.23517919999999},"viewport":{"northeast":{"lat":41.50445,"lng":-73.2195899},"southwest":{"lat":41.465295,"lng":-73.2513191}}}"""

        //     val jView = """{"northeast":{"lat":41.5142079,"lng":-73.155652},"southwest":{"lat":41.4213049,"lng":-73.32711399999999}}"""

        //     val jLoc = """{"lat":41.50445,"lng":-73.2195899}"""

        //     // val res: Coords = Json.parse(jLoc).as[Coords]
        //     val res: Geometry = Json.parse(jGeo).as[Geometry]

        //     println("\n")
        //     println(res)
        //     println("\n")
        //     success
        // }

        tag("json")
        "Decode json result" in new WithApplication {

            val part: String = """{"long_name" : "Southbury","short_name" : "Southbury","types" : [ "locality", "political" ]}"""

            val comps: String = """{"formatted_address" : "100 Poverty Rd, Southbury, CT 06488, USA","address_components" : [{"long_name" : "100","short_name" : "100","types" : [ "street_number" ]},{"long_name" : "Poverty Road","short_name" : "Poverty Rd","types" : [ "route" ]},{"long_name" : "Southbury","short_name" : "Southbury","types" : [ "locality", "political" ]},{"long_name" : "Connecticut","short_name" : "CT","types" : [ "administrative_area_level_1", "political" ]},{"long_name" : "United States","short_name" : "US","types" : [ "country", "political" ]},{"long_name" : "06488","short_name" : "06488","types" : [ "postal_code" ]}]}"""

            val fullRes: String = """{"address_components" : [{"long_name" : "100","short_name" : "100","types" : [ "street_number" ]},{"long_name" : "Poverty Road","short_name" : "Poverty Rd","types" : [ "route" ]},{"long_name" : "Southbury","short_name" : "Southbury","types" : [ "locality", "political" ]},{"long_name" : "Connecticut","short_name" : "CT","types" : [ "administrative_area_level_1", "political" ]},{"long_name" : "United States","short_name" : "US","types" : [ "country", "political" ]},{"long_name" : "06488","short_name" : "06488","types" : [ "postal_code" ]}],"formatted_address" : "100 Poverty Rd, Southbury, CT 06488, USA","geometry" : {"bounds" : {"northeast" : {"lat" : 41.4806915,"lng" : -73.21958870000002},"southwest" : {"lat" : 41.4801182,"lng" : -73.2205256}},"location" : {"lat" : 41.4804048,"lng" : -73.2200572},"location_type" : "ROOFTOP","viewport" : {"northeast" : {"lat" : 41.4817538302915,"lng" : -73.21870816970851},"southwest" : {"lat" : 41.4790558697085,"lng" : -73.22140613029153}}},"place_id" : "ChIJHY3rpXHv54kRcKH71a5Jk4Y","types" : [ "premise" ]}"""

            val res: GeocodeParsed = Json.parse(fullRes).as[GeocodeParsed]

            println("")
            println(res)
            println("")

            success
        }


        tag("geocode")
        "Send a geocode request" in new WithApplication {

            Play.current.configuration.getString("googleApis.geocode.key").map { key =>

                val geocodeReq: WSRequest = WS.url("https://maps.googleapis.com/mapssefsse/api/geocode/json")
                    .withQueryString(
                        "address" -> "100 Poverty Rd, Southbury, CT 06488"
                    )
                        // "key" -> key
                        // "address" -> "118737642 Poverty Rd, Southbury, CT 06488",

                sendReq(geocodeReq)

                success
            }
        }


        tag("nearby")
        "Send a nearby search request" in new WithApplication {

            Play.current.configuration.getString("googleApis.places.key").map { key =>
                val placesReq = WS.url("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                    .withQueryString(
                        "location" -> loc.mkString,
                        "radius" -> "1000"
                    )
                        // "key" -> key,

                sendReq(placesReq)

                success
            }
        }

        // "Send a Request" in new WithApplication {


        //     Play.current.configuration.getString("googleApis.places.key").map { key =>

        //         val radarReq = WS.url("https://maps.googleapis.com/maps/api/place/radarsearch/json")
        //             .withQueryString(
        //                 "location" -> loc.mkString,
        //                 "key" -> key,
        //                 "radius" -> "1000",
        //                 "type" -> "establishment"
        //             )

        //         sendReq(radarReq)
        //     }
        //     success
        // }
    }

    def sendReq(ws: WSRequest): Unit = {

        val res: Future[WSResponse] = ws.get()
        val result = Await.result(res, Duration(8000, "millis"))

        println("\n----------")
        println()
        println("-----------\n\n")
        println(result)
        println(" ---- ")
        println(result.body)
        println(" ---- ")
        println(result.json)
        println(" ---- ")
    }
}