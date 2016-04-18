package test.unit.libs

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import sts.util.debug.helpers._
import scala.util.{Try, Success, Failure}
import sts.libs.functional.{FunctionFragment, PiecewiseFunction}

class FunctionFragmentSpec extends Specification {


    t
    "Function-Fragments" should {

        trait Thing { def id: Int }
        case class Temp(id: Int) extends Thing
        case class Junk(id: Int, name: String) extends Thing

        val pfTemp = PartialFunction[Thing, String] {
            case temp: Temp if(temp.id % 2 == 0) => "Temp(even)"
        }

        val pfJunk = PartialFunction[Thing, String] {
            case junk: Junk if(junk.name.length % 2 == 0) => "Junk(even)"
        }

br
        "Return false from `isDefinedAt` if input fails pattern match" in {
            val ffJunk = FunctionFragment.fromPartial(pfJunk)

            ffJunk.isDefinedAt(new Thing {val id = 2}) must beFalse
            ffJunk.isDefinedAt(Junk(2, "abc")) must beFalse
            ffJunk.isDefinedAt(Temp(2)) must beFalse

            ffJunk.isDefinedAt(Junk(3, "abcd")) must beTrue
        }
br
        "Lift into a function that returns None instead of throwing match exceptions" in {
            val liftJunk = FunctionFragment.fromPartial(pfJunk).lift

            liftJunk(Temp(2)) must beNone
            liftJunk(Junk(3, "abcd")) must beSome("Junk(even)")
        }
br
        "Create a new `FunctionFragment` by combining two others" in {
            val ffTemp = FunctionFragment.fromPartial(pfTemp)
            val ffJunk = FunctionFragment.fromPartial(pfJunk)

            val ffBoth = ffTemp + ffJunk

            ffBoth(Temp(2)) must equalTo("Temp(even)")
            ffBoth(Junk(2, "abcd")) must equalTo("Junk(even)")

            ffBoth.isDefinedAt(Junk(2, "abc")) must beFalse
            ffBoth.isDefinedAt(new Thing {val id = 2}) must beFalse
        }
    }

br
br
    "Piecewise-Functions" should {

        val anyMatchPartials: Seq[PartialFunction[Any, String]] = Seq(
            PartialFunction[Any, String] { case a: String => "String-Match" },
            PartialFunction[Any, String] { case a: Char => "Char-Match" },
            PartialFunction[Any, String] { case a: Long => "Long-Match" },
            PartialFunction[Any, String] { case a: Int => "Int-Match" }
        )

        val catchAll = PartialFunction[Any, String] { case _ => "Any Non-match" }

        val wasMatchFound = (result: String) => (result containsSlice "Match")

        val preOp = (arg: String) => arg match {
            case "int" => 0
            case "long" => 0L
            case "string" => ""
            case "char" => 'A'
            case _ => 0.0
        }
br
        "Construct a `PiecewiseFunction` from a list of `PartialFunction`s" in {

            val piecewise = PiecewiseFunction[Any, String](anyMatchPartials: _ *)

            Verify anyPartialsExpected piecewise
        }
br
        "Append the parts of one `PiecewiseFunction` to another" in {

            val pwise1 = PiecewiseFunction[Any, String](anyMatchPartials.take(2): _ *)
            val pwise2 = PiecewiseFunction[Any, String](anyMatchPartials.drop(2): _ *)
            val piecewise = (pwise1 ++ pwise2)

            Verify anyPartialsExpected piecewise
        }
br
        "Append and Prepend `PartialFunction`s to a `PiecewiseFunction`" in {
            val pwise0 = PiecewiseFunction[Any, String](anyMatchPartials: _ *)
            val piecewise = pwise0 :+ catchAll

            Verify anyPartialsExpected piecewise
            piecewise(0.0) must equalTo("Any Non-match")

            val backwards = pwise0.+:(catchAll)

            List[Any]("abcd", 'A', 1L, 1).map(backwards) must contain(
                equalTo("Any Non-match")
            ).exactly(4.times)
        }
br
        "Lift a `PiecewiseFunction` into a regular function" in {

            val liftedPiecewise =
                PiecewiseFunction[Any, String](anyMatchPartials: _ *).lift

            liftedPiecewise('A') aka "the result of a defined input" must beSome("Char-Match")

            liftedPiecewise(0.0) aka "the result of an undefined input" must beNone
        }
br
        "Attach a function to the result of a `PiecewiseFunction` with `andThen`" in {
            val piecewise = PiecewiseFunction[Any, String](
                (anyMatchPartials :+ catchAll): _ *
            )
            val andthend = piecewise andThen wasMatchFound

            andthend(5) must beTrue
            andthend(0.0) must beFalse
        }
br
        "Add a compose step to a `PiecewiseFunction`" in {
            val piecewise = PiecewiseFunction[Any, String](anyMatchPartials: _ *)

            val composed = piecewise compose preOp

            composed("int") must equalTo("Int-Match")
            composed("char") must equalTo("Char-Match")
        }
br
    }

    object Verify {

        def anyPartialsExpected(piecewise: PiecewiseFunction[Any, String]) = {
            piecewise("abcd") must equalTo("String-Match")
            piecewise('Z') must equalTo("Char-Match")
            piecewise(1L) must equalTo("Long-Match")
            piecewise(1) must equalTo("Int-Match")
        }
    }
}