package sts.util


/* Moritz - stackoverflow.com */
package object Convertable {

    private case object NoConversion extends (Any => Nothing) {
       def apply(x: Any) = sys.error("No conversion")
    }

    // Just for convenience so NoConversion does not escape the scope.
    private def noConversion: Any => Nothing = NoConversion

    // sts: f: A => B *equals default function*
    // and now some convenience methods that can be safely exposed:
    def canConvert[A,B]()(implicit f: A => B = noConversion) =
        (f ne NoConversion)

    def tryConvert[A,B](a: A)(implicit f: A => B = noConversion): Either[A,B] =
        if (f eq NoConversion) Left(a) else Right(f(a))

    def optConvert[A,B](a: A)(implicit f: A => B = noConversion): Option[B] =
        if (f ne NoConversion) Some(f(a)) else None
}

import play.api.libs.json.{JsValue, Reads, Writes}
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

package object Jsonables {

    def hasReads[A](implicit rd: Reads[A] = null) =
        (rd ne null)

    def hasWrites[A](implicit wrt: Writes[A] = null) =
        (wrt ne null)

    def readsOption[A](implicit rd: Reads[A] = null): Option[Reads[A]] =
        rd match {
            case null => None
            case _ => Some(rd)
        }

    def writesOption[A](implicit wrt: Writes[A] = null): Option[Writes[A]] =
        wrt match {
            case null => None
            case _ => Some(wrt)
        }

    def pairReads[A](o: A): (A, Option[Reads[A]]) = (o -> readsOption[A])

    def pairWrites[A](o: A): (A, Option[Writes[A]]) = (o -> writesOption[A])

    object Implicits {

        implicit class IterableExt[A, M[A] <: Iterable[A]](l: M[A]) {

            def zipWithReads[B >: A](
                implicit bf: CanBuildFrom[ Iterable[(A, Option[Reads[A]])], (A, Reads[A]), M[(B, Reads[B])] ]

            ): M[(B, Reads[B])] = l.map(pairReads).collect {
                    case (o, Some(rd)) => (o, rd)
            }(bf)


            def zipWithWrites[B >: A](
                implicit bf: CanBuildFrom[ Iterable[(A, Option[Writes[A]])], (A, Writes[A]), M[(B, Writes[B])] ]

            ): M[(B, Writes[B])] = l.map(pairWrites).collect {
                    case (o, Some(wrt)) => (o, wrt)
            }(bf)
        }
    }
}