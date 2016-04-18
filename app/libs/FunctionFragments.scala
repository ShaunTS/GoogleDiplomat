package sts.libs.functional

import scala.util.{Failure, Success, Try}

trait FunctionFragment[-A, +B] extends PartialFunction[A, B] {

    import FunctionFragment._

    override def isDefinedAt(x: A): Boolean = {
        Try(this(x)) match {
            case Failure(e: MatchError) => false
            case Success(_) => true
            case Failure(_) => true
        }
    }

    def orElse[A1 <: A, B1 >: B](that: FunctionFragment[A1, B1]): FunctionFragment[A1, B1] =
        fromPartial(super.orElse(that))

    def +[A1 <: A, B1 >: B](that: FunctionFragment[A1, B1]): FunctionFragment[A1, B1] =
        this.orElse(that)

    override def andThen[C](k: B => C): FunctionFragment[A, C] =
        fromPartial(super.andThen(k))
}

object FunctionFragment {

    def apply[A, B](f: A => B): FunctionFragment[A, B] = new FunctionFragment[A, B] {

        def apply(x: A) = f(x)
    }

    def fromPartial[A, B](pf: PartialFunction[A, B]): FunctionFragment[A, B] =
        new FunctionFragment[A, B] {
            def apply(x: A) = pf(x)
        }
}

trait PiecewiseFunction[-A, +B] extends FunctionFragment[A, B] { self =>

    import FunctionFragment.fromPartial

    def fs: Seq[FunctionFragment[A, B]]

    override def apply(x: A): B = (this.fs.reduce(_ orElse _))(x)

    def orElse[A1 <: A, B1 >: B](that: PiecewiseFunction[A1, B1]): PiecewiseFunction[A1, B1] =
        new PiecewiseFunction[A1, B1] {
            val fs: Seq[FunctionFragment[A1, B1]] = self.fs ++ that.fs
        }

    override def orElse[A1 <: A, B1 >: B](pf: PartialFunction[A1, B1]): PiecewiseFunction[A1, B1] =
        new PiecewiseFunction[A1, B1] {
            val fs: Seq[FunctionFragment[A1, B1]] = self.fs :+ fromPartial(pf)
        }

    def ++[A1 <: A, B1 >: B](that: PiecewiseFunction[A1, B1]): PiecewiseFunction[A1, B1] = (this orElse that)

    def :+[A1 <: A, B1 >: B](pf: PartialFunction[A1, B1]): PiecewiseFunction[A1, B1] = (this orElse pf)

    def +:[A1 <: A, B1 >: B](pf: PartialFunction[A1, B1]): PiecewiseFunction[A1, B1] =
        new PiecewiseFunction[A1, B1] {
            val fs: Seq[FunctionFragment[A1, B1]] = self.fs.+:(fromPartial(pf))
        }
}

object PiecewiseFunction {

    def apply[A, B](pieces: PartialFunction[A, B] *): PiecewiseFunction[A, B] = new PiecewiseFunction[A, B] {
        val fs: Seq[FunctionFragment[A, B]] = pieces.map(FunctionFragment.fromPartial(_))
    }
}