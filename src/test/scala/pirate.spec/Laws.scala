package pirate.spec

import scalaz._
import Scalaz._

import hedgehog._
import hedgehog.runner._

/* ripped from scalaz-scalacheck-binding due to binary compatability issues */

object Laws extends LawsCompat {
  object equal {
    def commutativity[A](genA: Gen[A])(implicit A: Equal[A]): Property = for {
      a <- genA.log("a")
      b <- genA.log("b")
    } yield {
      Result.assert(A.equalLaw.commutative(a, b))
    }

    def reflexive[A](genA: Gen[A])(implicit A: Equal[A]): Property = for {
      a <- genA.log("a")
    } yield {
      Result.assert(A.equalLaw.reflexive(a))
    }

    def transitive[A](genA: Gen[A])(implicit A: Equal[A]): Property = for {
      a <- genA.log("a")
      b <- genA.log("b")
      c <- genA.log("c")
    } yield {
      Result.assert(A.equalLaw.transitive(a, b, c))
    }

    def naturality[A](genA: Gen[A])(implicit A: Equal[A]): Property = for {
      a <- genA.log("a")
      b <- genA.log("b")
    } yield {
      Result.assert(A.equalLaw.naturality(a, b))
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
      Result.assert(A.semigroupLaw.associative(a, b, c))
    }

    def laws[A](name: String, genA: Gen[A])(implicit A: Semigroup[A], eqa: Equal[A]): List[Test] = List(
      property(s"$name - Semigroup Law: associative", associative(genA))
    )
  }


  object monoid {
    def leftIdentity[A](genA: Gen[A])(implicit A: Monoid[A], eqa: Equal[A]): Property = for {
      a <- genA.log("a")
    } yield {
      Result.assert(A.monoidLaw.leftIdentity(a))
    }

    def rightIdentity[A](genA: Gen[A])(implicit A: Monoid[A], eqa: Equal[A]): Property = for {
      a <- genA.log("a")
    } yield {
      Result.assert(A.monoidLaw.rightIdentity(a))
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
    def identity[F[_], A](genFA: Gen[F[A]])(implicit F: Functor[F], ef: Equal[F[A]]): Property = for {
      fa <- genFA.log("fa")
    } yield {
      Result.assert(F.functorLaw.identity[A](fa))
    }

    def composite[F[_], A, B, C](
      genFA: Gen[F[A]], genAB: Gen[A => B], genBC: Gen[B => C]
    )(
      implicit F: Functor[F], ef: Equal[F[C]]
    ): Property = for {
      fa <- genFA.log("fa")
      ab <- genAB.log("ab")
      bc <- genBC.log("bc")
    } yield {
      Result.assert(F.functorLaw.composite[A, B, C](fa, ab, bc))
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
    def identity[F[_], A](genFA: Gen[F[A]])(implicit f: Applicative[F], ef: Equal[F[A]]): Property =
      for {
        fa <- genFA.log("fa")
      } yield {
        Result.assert(f.applicativeLaw.identityAp[A](fa))
      }

    def composition[F[_], A, B, C](
      genFA: Gen[F[A]], genFBC: Gen[F[B => C]], genFAB: Gen[F[A => B]]
    )(
      implicit ap: Applicative[F], e: Equal[F[C]]
    ): Property = for {
      fa <- genFA.log("fa")
      fbc <- genFBC.log("fbc")
      fab <- genFAB.log("fab")
    } yield {
      Result.assert(ap.applicativeLaw.composition[A, B, C](fbc, fab, fa))
    }

    def homomorphism[F[_], A, B](
      genA: Gen[A], genAB: Gen[A => B]
    )(
      implicit ap: Applicative[F], e: Equal[F[B]]
    ): Property = for {
      a <- genA.log("a")
      ab <- genAB.log("ab")
    } yield {
      Result.assert(ap.applicativeLaw.homomorphism[A, B](ab, a))
    }

    def interchange[F[_], A, B](
      genA: Gen[A], genFAB: Gen[F[A => B]]
    )(
      implicit ap: Applicative[F], e: Equal[F[B]]
    ): Property = for {
      a <- genA.log("a")
      fab <- genFAB.log("fab")
    } yield {
      Result.assert(ap.applicativeLaw.interchange[A, B](fab, a))
    }

    def mapApConsistency[F[_], A, B](
      genFA: Gen[F[A]], genAB: Gen[A => B]
    )(
      implicit ap: Applicative[F], e: Equal[F[B]]
    ): Property = for {
      fa <- genFA.log("fa")
      ab <- genAB.log("ab")
    } yield {
      Result.assert(ap.applicativeLaw.mapLikeDerived[A, B](ab, fa) )
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
    def rightIdentity[M[_], A](genMA: Gen[M[A]])(implicit M: Monad[M], e: Equal[M[A]]): Property =
      for {
        ma <- genMA.log("ma")
      } yield {
        Result.assert(M.monadLaw.rightIdentity[A](ma))
      }

    def leftIdentity[M[_], A, B](
      genA: Gen[A], genAToMB: Gen[A => M[B]]
    )(
      implicit am: Monad[M], emy: Equal[M[B]]
    ): Property =
      for {
        a <- genA.log("a")
        amb <- genAToMB.log("amb")
      } yield {
        Result.assert(am.monadLaw.leftIdentity[A, B](a, amb))
      }

    def associativity[M[_], A, B, C](
      genMA: Gen[M[A]]
    , genAToMB: Gen[A => M[B]]
    , genBToMC: Gen[B => M[C]]
    )(
      implicit M: Monad[M], emz: Equal[M[C]]
    ): Property = for {
      ma <- genMA.log("ma")
      amb <- genAToMB.log("amb")
      bmc <- genBToMC.log("bmc")
    } yield {
      Result.assert(M.monadLaw.associativeBind[A, B, C](ma, amb, bmc))
    }

    def bindApConsistency[M[_], A, B](
      genMA: Gen[M[A]]
    , genMAB: Gen[M[A => B]]
    )(
      implicit M: Monad[M], emy: Equal[M[B]]
    ): Property = for {
      ma <- genMA.log("ma")
      mab <- genMAB.log("mab")
    } yield {
      Result.assert(M.monadLaw.apLikeDerived[A, B](ma, mab))
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
    def identityTraverse[F[_], A, B](
      genFA: Gen[F[A]]
    , genAB: Gen[A => B]
    )(
      implicit f: Traverse[F], ef: Equal[F[B]]
    ): Property = for {
      fa <- genFA.log("fa")
      ab <- genAB.log("ab")
    } yield {
      Result.assert(f.traverseLaw.identityTraverse[A, B](fa, ab))
    }

    def purity[F[_], G[_], A](
      genFA: Gen[F[A]]
    )(
      implicit f: Traverse[F], G: Applicative[G], ef: Equal[G[F[A]]]
    ): Property = for {
      fa <- genFA.log("fa")
    } yield {
      Result.assert(f.traverseLaw.purity[G, A](fa))
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
      Result.assert(F.traverseLaw.sequentialFusion[N, M, A, B, C](fa, amb, bnc))
    }

    def naturality[F[_], N[_], M[_], A](nat: (M ~> N))(genFMA: Gen[F[M[A]]])(
      implicit F: Traverse[F], N: Applicative[N], M: Applicative[M], NFA: Equal[N[F[A]]]
    ): Property = for {
      fma <- genFMA.log("fma")
    } yield {
      Result.assert(F.traverseLaw.naturality[N, M, A](nat)(fma))
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
      Result.assert(F.traverseLaw.parallelFusion[N, M, A, B](fa, amb, anb))
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
      , property(s"$name - $lawName: purity.lazyCollection", purity[F, LazyCollection, Int](genFInt))
      , property(s"$name - $lawName: sequential fusion", sequentialFusion[F, Option, List, Int, Int, Int](genFInt, genIntToListInt, genIntToOptionInt))
      )
    }
  }


  object plus {
    def associative[F[_], A](genFA: Gen[F[A]])(implicit f: Plus[F], ef: Equal[F[A]]): Property = for {
      fa1 <- genFA.log("fa1")
      fa2 <- genFA.log("fa2")
      fa3 <- genFA.log("fa3")
    } yield {
      Result.assert(f.plusLaw.associative[A](fa1, fa2, fa3))
    }

    def laws[F[_]](name: String, genFInt: Gen[F[Int]])(implicit F: Plus[F], ef: Equal[F[Int]]): List[Test] =
      semigroup.laws[F[Int]](name, genFInt)(F.semigroup[Int], implicitly) ++
      List(
        property(s"$name - Plus Law: associative", associative[F, Int](genFInt))
      )
  }

  object plusEmpty {
    def leftPlusIdentity[F[_], A](genFA: Gen[F[A]])(implicit f: PlusEmpty[F], ef: Equal[F[A]]): Property =
      for {
        fa <- genFA.log("fa")
      } yield {
        Result.assert(f.plusEmptyLaw.leftPlusIdentity[A](fa))
      }

    def rightPlusIdentity[F[_], A](genFA: Gen[F[A]])(implicit f: PlusEmpty[F], ef: Equal[F[A]]): Property =
      for {
        fa <- genFA.log("fa")
      } yield {
        Result.assert(f.plusEmptyLaw.rightPlusIdentity[A](fa))
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
    def emptyMap[F[_], A](genAToA: Gen[A => A])(implicit f: MonadPlus[F], ef: Equal[F[A]]): Property =
      for {
      aa <- genAToA.log("aa")
      } yield {
        Result.assert(f.monadPlusLaw.emptyMap[A](aa))
      }

    def leftZero[F[_], A](genAToFA: Gen[A => F[A]])(implicit F: MonadPlus[F], ef: Equal[F[A]]): Property =
      for {
        afa <- genAToFA.log("afa")
      } yield {
        Result.assert(F.monadPlusLaw.leftZero[A](afa))
      }

    def rightZero[F[_], A](genFA: Gen[F[A]])(implicit F: MonadPlus[F], ef: Equal[F[A]]): Property =
      for {
        fa <- genFA.log("fa")
      } yield {
        Result.assert(F.strongMonadPlusLaw.rightZero[A](fa))
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
