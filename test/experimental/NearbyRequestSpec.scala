package test.unit.models

import sts.diplomat.models._

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

object NearbyRequestSpec extends Specification {


    "The Nearby Search Request model" should {

        val library = Coords(41.4804518, -73.2200572)

        val req = NearbyRequest.empty.copy(
            loc = library,
            radius = 1000
        )

        "Create a Nearby Request" in new WithApplication {

            val res = NearbyRequest.create(req)

            println("------------------------------------------")
            // println(req.extraParams.stringOpt)
            println("next id = " + res)
            println("------------------------------------------")

            success
        }
    }
}