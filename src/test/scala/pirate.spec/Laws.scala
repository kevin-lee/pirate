package pirate.spec

import scalaz._
import Scalaz._

import hedgehog._
import hedgehog.runner._

/* ripped from scalaz-scalacheck-binding due to binary compatability issues */

object Laws {
  object equal {
    def commutativity[A](genA: Gen[A])(implicit A: Equal[A]): Property = for {
      a <- genA.log("a")
      b <- genA.log("b")
    } yield {
      A.equalLaw.commutative(a, b) ==== true
    }

    def reflexive[A](genA: Gen[A])(implicit A: Equal[A]): Property = for {
      a <- genA.log("a")
    } yield {
      A.equalLaw.reflexive(a) ==== true
    }

    def transitive[A](genA: Gen[A])(implicit A: Equal[A]): Property = for {
      a <- genA.log("a")
      b <- genA.log("b")
      c <- genA.log("c")
    } yield {
      A.equalLaw.transitive(a, b, c) ==== true
    }

    def naturality[A](genA: Gen[A])(implicit A: Equal[A]): Property = for {
      a <- genA.log("a")
      b <- genA.log("b")
    } yield {
      A.equalLaw.naturality(a, b) ==== true
    }

    def laws[A](name: String, genA: Gen[A])(implicit A: Equal[A]): List[Test] = {
      val lawName = "Equal Law"
      List(
        property(s"$name - $lawName: commutativity", commutativity(genA))
      , property(s"$name - $lawName: reflexive",  reflexive(genA))
      , property(s"$name - $lawName: transitive", transitive(genA))
      , property(s"$name - $lawName: naturality", naturality(genA))
      )
    }
  }

  object semigroup {
    def associative[A](genA: Gen[A])(implicit A: Semigroup[A], eqa: Equal[A]): Property = for {
      a <- genA.log("a")
      b <- genA.log("b")
      c <- genA.log("c")
    } yield {
      A.semigroupLaw.associative(a, b, c) ==== true
    }

    def laws[A](name: String, genA: Gen[A])(implicit A: Semigroup[A], eqa: Equal[A]): List[Test] = List(
      property(s"$name - Semigroup Law: associative", associative(genA))
    )
  }


  object monoid {
    def leftIdentity[A](genA: Gen[A])(implicit A: Monoid[A], eqa: Equal[A]): Property = for {
      a <- genA.log("a")
    } yield {
      A.monoidLaw.leftIdentity(a) ==== true
    }

    def rightIdentity[A](genA: Gen[A])(implicit A: Monoid[A], eqa: Equal[A]): Property = for {
      a <- genA.log("a")
    } yield {
      A.monoidLaw.rightIdentity(a) ==== true
    }

    def laws[A](name: String, genA: Gen[A])(implicit A: Monoid[A], eqa: Equal[A]): List[Test] = {
      val lawName = "Monoid Law"
      semigroup.laws(name, genA) ++ List(
        property(s"$name - $lawName: left identity", leftIdentity(genA))
      , property(s"$name - $lawName: right identity", rightIdentity(genA))
      )
    }
  }

  object functor {
    def identity[F[_], X](genFX: Gen[F[X]])(implicit F: Functor[F], ef: Equal[F[X]]): Property = for {
      fa <- genFX.log("fa")
    } yield {
      F.functorLaw.identity[X](fa) ==== true
    }

    def composite[F[_], X, Y, Z](
      genFX: Gen[F[X]], genXY: Gen[X => Y], genYZ: Gen[Y => Z]
    )(
      implicit F: Functor[F], ef: Equal[F[Z]]
    ): Property = for {
      fa <- genFX.log("fa")
      ab <- genXY.log("ab")
      bc <- genYZ.log("bc")
    } yield {
      F.functorLaw.composite[X, Y, Z](fa, ab, bc)==== true
    }

    def laws[F[_]](
      name: String
    , genFInt: Gen[F[Int]]
    , genIntToInt: Gen[Int => Int]
    )(
      implicit F: Functor[F], ef: Equal[F[Int]]
    ): List[Test] = {
      val lawName = "Functor Law"
      List(
        property(s"$name - $lawName: identity", identity[F, Int](genFInt))
      , property(s"$name - $lawName: composite", composite[F, Int, Int, Int](genFInt, genIntToInt, genIntToInt))
      )
    }
  }

  object applicative {
    def identity[F[_], X](genFX: Gen[F[X]])(implicit f: Applicative[F], ef: Equal[F[X]]): Property =
      for {
        fa <- genFX.log("fa")
      } yield {
        f.applicativeLaw.identityAp[X](fa) ==== true
      }

    def composition[F[_], X, Y, Z](
      genFX: Gen[F[X]], genFYZ: Gen[F[Y => Z]], genFXY: Gen[F[X => Y]]
    )(
      implicit ap: Applicative[F], e: Equal[F[Z]]
    ): Property = for {
      fa <- genFX.log("fa")
      fbc <- genFYZ.log("fbc")
      fab <- genFXY.log("fab")
    } yield {
      ap.applicativeLaw.composition[X, Y, Z](fbc, fab, fa) ==== true
    }

    def homomorphism[F[_], X, Y](
      genX: Gen[X], genXY: Gen[X => Y]
    )(
      implicit ap: Applicative[F], e: Equal[F[Y]]
    ): Property = for {
      a <- genX.log("a")
      ab <- genXY.log("ab")
    } yield {
      ap.applicativeLaw.homomorphism[X, Y](ab, a) ==== true
    }

    def interchange[F[_], X, Y](
      genX: Gen[X], genFXY: Gen[F[X => Y]]
    )(
      implicit ap: Applicative[F], e: Equal[F[Y]]
    ): Property = for {
      a <- genX.log("a")
      fab <- genFXY.log("fab")
    } yield {
      ap.applicativeLaw.interchange[X, Y](fab, a) ==== true
    }

    def mapApConsistency[F[_], X, Y](
      genFX: Gen[F[X]], genXY: Gen[X => Y]
    )(
      implicit ap: Applicative[F], e: Equal[F[Y]]
    ): Property = for {
      fa <- genFX.log("fa")
      ab <- genXY.log("ab")
    } yield {
      ap.applicativeLaw.mapLikeDerived[X, Y](ab, fa)  ==== true
    }

    def laws[F[_]](
        name: String
      , genInt: Gen[Int]
      , genIntToInt: Gen[Int => Int]
      , genFInt: Gen[F[Int]]
      , genFIntToInt: Gen[F[Int => Int]]
      )(
        implicit F: Applicative[F], e: Equal[F[Int]]
      ): List[Test] = {
        val lawName = "Applicative Law"
        functor.laws[F](name, genFInt, genIntToInt) ++
        List(
          property(s"$name - $lawName: identity", applicative.identity[F, Int](genFInt))
        , property(s"$name - $lawName: composition", applicative.composition[F, Int, Int, Int](genFInt, genFIntToInt, genFIntToInt))
        , property(s"$name - $lawName: homomorphism", applicative.homomorphism[F, Int, Int](genInt, genIntToInt))
        , property(s"$name - $lawName: interchange", applicative.interchange[F, Int, Int](genInt, genFIntToInt))
        , property(s"$name - $lawName: map consistent with ap", applicative.mapApConsistency[F, Int, Int](genFInt, genIntToInt))
        )
      }
  }

  object monad {
    def rightIdentity[M[_], X](genMX: Gen[M[X]])(implicit M: Monad[M], e: Equal[M[X]]): Property =
      for {
        ma <- genMX.log("ma")
      } yield {
        M.monadLaw.rightIdentity[X](ma) ==== true
      }

    def leftIdentity[M[_], X, Y](
      genX: Gen[X], genXMY: Gen[X => M[Y]]
    )(
      implicit am: Monad[M], emy: Equal[M[Y]]
    ): Property =
      for {
        a <- genX.log("a")
        amb <- genXMY.log("amb")
      } yield {
        am.monadLaw.leftIdentity[X, Y](a, amb) ==== true
      }

    def associativity[M[_], X, Y, Z](
      genMX: Gen[M[X]]
    , genXMY: Gen[X => M[Y]]
    , genYMZ: Gen[Y => M[Z]]
    )(
      implicit M: Monad[M], emz: Equal[M[Z]]
    ): Property = for {
      ma <- genMX.log("ma")
      amb <- genXMY.log("amb")
      bmc <- genYMZ.log("bmc")
    } yield {
      M.monadLaw.associativeBind[X, Y, Z](ma, amb, bmc) ==== true
    }

    def bindApConsistency[M[_], X, Y](
      genMX: Gen[M[X]]
    , genMXY: Gen[M[X => Y]]
    )(
      implicit M: Monad[M], emy: Equal[M[Y]]
    ): Property = for {
      ma <- genMX.log("ma")
      mab <- genMXY.log("mab")
    } yield {
      M.monadLaw.apLikeDerived[X, Y](ma, mab) ==== true
    }

    def laws[M[_]](
        name: String
      , genInt: Gen[Int]
      , genIntToInt: Gen[Int => Int]
      , genMInt: Gen[M[Int]]
      , genIntToMInt: Gen[Int => M[Int]]
      , genMIntToInt: Gen[M[Int => Int]]
      )(
        implicit a: Monad[M], e: Equal[M[Int]]
      ): List[Test] = {
        val lawName = "Monad Law"
        applicative.laws[M](name, genInt, genIntToInt, genMInt, genMIntToInt) ++
        List (
          property(s"$name - $lawName: right identity", monad.rightIdentity[M, Int](genMInt))
        , property(s"$name - $lawName: left identity", monad.leftIdentity[M, Int, Int](genInt, genIntToMInt))
        , property(s"$name - $lawName: associativity", monad.associativity[M, Int, Int, Int](genMInt, genIntToMInt, genIntToMInt))
        , property(s"$name - $lawName: ap consistent with bind", monad.bindApConsistency[M, Int, Int](genMInt, genMIntToInt))
        )
      }
  }

  object traverse {
    def identityTraverse[F[_], X, Y](
      genFX: Gen[F[X]]
    , genXY: Gen[X => Y]
    )(
      implicit f: Traverse[F], ef: Equal[F[Y]]
    ): Property = for {
      fa <- genFX.log("fa")
      ab <- genXY.log("ab")
    } yield {
      f.traverseLaw.identityTraverse[X, Y](fa, ab) ==== true
    }

    def purity[F[_], G[_], X](
      genFX: Gen[F[X]]
    )(
      implicit f: Traverse[F], G: Applicative[G], ef: Equal[G[F[X]]]
    ): Property = for {
      fa <- genFX.log("fa")
    } yield {
      f.traverseLaw.purity[G, X](fa) ==== true
    }

    def sequentialFusion[F[_], N[_], M[_], A, B, C](
      genFA: Gen[F[A]]
    , genAMB: Gen[A => M[B]]
    , genBNC: Gen[B => N[C]]
    )(
      implicit F: Traverse[F], N: Applicative[N], M: Applicative[M], MN: Equal[M[N[F[C]]]]
    ): Property = for {
      fa <- genFA.log("fa")
      amb <- genAMB.log("amb")
      bnc <- genBNC.log("bnc")
    } yield {
      F.traverseLaw.sequentialFusion[N, M, A, B, C](fa, amb, bnc) ==== true
    }

    def naturality[F[_], N[_], M[_], A](nat: (M ~> N))(genFMA: Gen[F[M[A]]])(
      implicit F: Traverse[F], N: Applicative[N], M: Applicative[M], NFA: Equal[N[F[A]]]
    ): Property = for {
      fma <- genFMA.log("fma")
    } yield {
      F.traverseLaw.naturality[N, M, A](nat)(fma) ==== true
    }

    def parallelFusion[F[_], N[_], M[_], A, B](
      genFA: Gen[F[A]]
    , genAMB: Gen[A => M[B]]
    , genANB: Gen[A => N[B]]
    )(
      implicit F: Traverse[F], N: Applicative[N], M: Applicative[M], MN: Equal[(M[F[B]], N[F[B]])]
    ): Property = for {
      fa <- genFA.log("fa")
      amb <- genAMB.log("amb")
      anb <- genANB.log("anb")
    } yield {
      F.traverseLaw.parallelFusion[N, M, A, B](fa, amb, anb) ==== true
    }

    def laws[F[_]](
      name: String
    , genFInt: Gen[F[Int]]
    , genIntToInt: Gen[Int => Int]
    , genIntToListInt: Gen[Int => List[Int]]
    , genIntToOptionInt: Gen[Int => Option[Int]]
    )(
      implicit F: Traverse[F], EF: Equal[F[Int]]
    ): List[Test] = {
      import std.list._, std.option._
      val lawName = "Traverse Law"
      List(
        property(s"$name - $lawName: identity traverse", identityTraverse[F, Int, Int](genFInt, genIntToInt))
      , property(s"$name - $lawName: purity.option", purity[F, Option, Int](genFInt))
      , property(s"$name - $lawName: purity.stream", purity[F, Stream, Int](genFInt))
      , property(s"$name - $lawName: sequential fusion", sequentialFusion[F, Option, List, Int, Int, Int](genFInt, genIntToListInt, genIntToOptionInt))
      )
    }
  }


  object plus {
    def associative[F[_], X](genFX: Gen[F[X]])(implicit f: Plus[F], ef: Equal[F[X]]): Property = for {
      fa1 <- genFX.log("fa1")
      fa2 <- genFX.log("fa2")
      fa3 <- genFX.log("fa3")
    } yield {
      f.plusLaw.associative[X](fa1, fa2, fa3) ==== true
    }

    def laws[F[_]](name: String, genFInt: Gen[F[Int]])(implicit F: Plus[F], ef: Equal[F[Int]]): List[Test] =
      semigroup.laws[F[Int]](name, genFInt)(F.semigroup[Int], implicitly) ++
      List(
        property(s"$name - Plus Law: associative", associative[F, Int](genFInt))
      )
  }

  object plusEmpty {
    def leftPlusIdentity[F[_], X](genFX: Gen[F[X]])(implicit f: PlusEmpty[F], ef: Equal[F[X]]): Property =
      for {
        fa <- genFX.log("fa")
      } yield {
        f.plusEmptyLaw.leftPlusIdentity[X](fa) ==== true
      }

    def rightPlusIdentity[F[_], X](genFX: Gen[F[X]])(implicit f: PlusEmpty[F], ef: Equal[F[X]]): Property =
      for {
        fa <- genFX.log("fa")
      } yield {
        f.plusEmptyLaw.rightPlusIdentity[X](fa) ==== true
      }

    def laws[F[_]](
      name: String, genFInt: Gen[F[Int]] , genIntToInt: Gen[Int => Int]
    )(
      implicit F: PlusEmpty[F], ef: Equal[F[Int]]
    ): List[Test] = {
      val lawName = "PlusEmpty Law"
      plus.laws[F](name, genFInt) ++
      monoid.laws[F[Int]](name, genFInt)(F.monoid[Int], implicitly) ++
      List(
        property(s"$name - $lawName: left plus identity", leftPlusIdentity[F, Int](genFInt))
      , property(s"$name - $lawName: right plus identity", rightPlusIdentity[F, Int](genFInt))
      )
    }
  }



  object monadPlus {
    def emptyMap[F[_], X](genXToX: Gen[X => X])(implicit f: MonadPlus[F], ef: Equal[F[X]]): Property =
      for {
      aa <- genXToX.log("aa")
      } yield {
        f.monadPlusLaw.emptyMap[X](aa) ==== true
      }

    def leftZero[F[_], X](genXToFX: Gen[X => F[X]])(implicit F: MonadPlus[F], ef: Equal[F[X]]): Property =
      for {
        afa <- genXToFX.log("afa")
      } yield {
        F.monadPlusLaw.leftZero[X](afa) ==== true
      }

    def rightZero[F[_], X](genFX: Gen[F[X]])(implicit F: MonadPlus[F], ef: Equal[F[X]]): Property =
      for {
        fa <- genFX.log("fa")
      } yield {
        F.strongMonadPlusLaw.rightZero[X](fa) ==== true
      }

    def laws[F[_]](
      name: String
    , genInt: Gen[Int]
    , genIntToInt: Gen[Int => Int]
    , genFInt: Gen[F[Int]]
    , genIntToFInt: Gen[Int => F[Int]]
    , genFIntToInt: Gen[F[Int => Int]]
    )(
      implicit F: MonadPlus[F], ef: Equal[F[Int]]
    ): List[Test] = {
      val lawName = "Monad plus Law"
      monad.laws[F](name, genInt, genIntToInt, genFInt, genIntToFInt, genFIntToInt) ++
      plusEmpty.laws[F](name, genFInt, genIntToInt) ++
      List(
        property(s"$name - $lawName: empty map", emptyMap[F, Int](genIntToInt))
      , property(s"$name - $lawName: left zero", leftZero[F, Int](genIntToFInt))
      )
    }

    def strongLaws[F[_]](
      name: String
    , genInt: Gen[Int]
    , genIntToInt: Gen[Int => Int]
    , genFInt: Gen[F[Int]]
    , genIntToFInt: Gen[Int => F[Int]]
    , genFIntToInt: Gen[F[Int => Int]]
    )(
      implicit F: MonadPlus[F], ef: Equal[F[Int]]
    ): List[Test] = {
      val lawName = "Monad plus Law"
      laws[F](name, genInt, genIntToInt, genFInt, genIntToFInt, genFIntToInt) ++
      List(
        property(s"$name - $lawName: right zero", rightZero[F, Int](genFInt))
      )
    }
  }

  object bifunctor {
    def laws[F[_, _]](
      name: String
    , genFIntInt: Gen[F[Int,Int]]
    , genIntToInt: Gen[Int => Int]
    )(
      implicit F: Bifunctor[F], E: Equal[F[Int, Int]]
    ): List[Test] =
      functor.laws[({type λ[α]=F[α, Int]})#λ](name, genFIntInt, genIntToInt)(F.leftFunctor[Int], implicitly) ++
        functor.laws[({type λ[α]=F[Int, α]})#λ](name, genFIntInt, genIntToInt)(F.rightFunctor[Int], implicitly)
  }
}
