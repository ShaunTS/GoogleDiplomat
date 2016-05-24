package test.unit.models

import org.specs2.matcher.DisjunctionMatchers
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import scalaz.{\/, \/-}
import sts.diplomat.models._
import sts.libs.errors._
import sts.test.WithSQL

import sts.util.debug.helpers._

object NearbyRequestSpec extends PlaySpecification with DisjunctionMatchers {

    import NearbyTestData._

    "The Nearby Search Request model" should {

        "Read and parse a NearbyRequest from DB" in new WithSQL(fakeRequests) {

            val result: \/[GenError, NearbyRequest] = NearbyRequest.find(id = 1L)

            result must be_\/-

            val \/-(request) = result

            request.id must beSome(1L)

            request.extraParams.flatten.size must be_==(3)

            val types = request.extraParams[List[String]]("types").toList.flatten

            types.length must be_==(6)
        }

        "Read a NearbySearch model with NULL json" in new WithSQL(fakeRequests) {

            val result: \/[GenError, NearbyRequest] = NearbyRequest.find(id = 2L)

            result must be_\/-

            val \/-(request) = result

            request.extraParams.flatten.size must be_==(0)
        }

        "Read a NearbyRequest model with empty json" in new WithSQL(fakeRequests) {

            val result: \/[GenError, NearbyRequest] = NearbyRequest.find(id = 3L)

            result must be_\/-

            val \/-(request) = result

            request.extraParams.flatten.size must be_==(0)
        }
    }
}

object NearbyTestData {

    val fakeRequests = "test/snapshots/nearbyRequests.sql"

    val library = Coords(41.4804518, -73.2200572)

    val libRequest = NearbyRequest.empty.copy(
        loc = library,
        radius = 750
    )
}