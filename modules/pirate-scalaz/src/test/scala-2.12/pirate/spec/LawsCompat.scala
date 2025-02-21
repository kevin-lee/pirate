package pirate.spec

import hedgehog._
import hedgehog.runner._

trait LawsCompat {
  type LazyCollection[+A] = Stream[A]

}
object LawsCompat extends LawsCompat {
  trait traverseCompat extends pirate.spec.LawsCommon.traverse {
    import scalaz._
    import Scalaz._

    def laws[F[_]](
      name: String,
      genFInt: Gen[F[Int]],
      genIntToInt: Gen[Int => Int],
      genIntToListInt: Gen[Int => List[Int]],
      genIntToOptionInt: Gen[Int => Option[Int]]
    )(
      implicit F: Traverse[F],
      EF: Equal[F[Int]]
    ): List[Test] = {
      import std.list._, std.option._
      val lawName = "Traverse Law"
      List(
        property(s"$name - $lawName: identity traverse", identityTraverse[F, Int, Int](genFInt, genIntToInt)),
        property(s"$name - $lawName: purity.option", purity[F, Option, Int](genFInt)),
        property(s"$name - $lawName: purity.ephemeralStream", purity[F, EphemeralStream, Int](genFInt)),
        property(s"$name - $lawName: purity.lazyCollection", purity[F, LazyCollection, Int](genFInt)),
        property(
          s"$name - $lawName: sequential fusion",
          sequentialFusion[F, Option, List, Int, Int, Int](genFInt, genIntToListInt, genIntToOptionInt)
        )
      )
    }

  }

}
