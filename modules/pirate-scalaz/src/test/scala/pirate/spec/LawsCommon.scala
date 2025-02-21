package pirate.spec

import hedgehog._
import scalaz._

/** @author Kevin Lee
  * @since 2025-02-21
  */
object LawsCommon {

  trait traverse {
    def identityTraverse[F[_], A, B](
      genFA: Gen[F[A]],
      genAB: Gen[A => B]
    )(
      implicit f: Traverse[F],
      ef: Equal[F[B]]
    ): Property = for {
      fa <- genFA.log("fa")
      ab <- genAB.log("ab")
    } yield {
      Result.assert(f.traverseLaw.identityTraverse[A, B](fa, ab))
    }

    def purity[F[_], G[_], A](
      genFA: Gen[F[A]]
    )(
      implicit f: Traverse[F],
      G: Applicative[G],
      ef: Equal[G[F[A]]]
    ): Property = for {
      fa <- genFA.log("fa")
    } yield {
      Result.assert(f.traverseLaw.purity[G, A](fa))
    }

    def sequentialFusion[F[_], N[_], M[_], A, B, C](
      genFA: Gen[F[A]],
      genAMB: Gen[A => M[B]],
      genBNC: Gen[B => N[C]]
    )(
      implicit F: Traverse[F],
      N: Applicative[N],
      M: Applicative[M],
      MN: Equal[M[N[F[C]]]]
    ): Property = for {
      fa  <- genFA.log("fa")
      amb <- genAMB.log("amb")
      bnc <- genBNC.log("bnc")
    } yield {
      Result.assert(F.traverseLaw.sequentialFusion[N, M, A, B, C](fa, amb, bnc))
    }

    def naturality[F[_], N[_], M[_], A](nat: (M ~> N))(genFMA: Gen[F[M[A]]])(
      implicit F: Traverse[F],
      N: Applicative[N],
      M: Applicative[M],
      NFA: Equal[N[F[A]]]
    ): Property = for {
      fma <- genFMA.log("fma")
    } yield {
      Result.assert(F.traverseLaw.naturality[N, M, A](nat)(fma))
    }

    def parallelFusion[F[_], N[_], M[_], A, B](
      genFA: Gen[F[A]],
      genAMB: Gen[A => M[B]],
      genANB: Gen[A => N[B]]
    )(
      implicit F: Traverse[F],
      N: Applicative[N],
      M: Applicative[M],
      MN: Equal[(M[F[B]], N[F[B]])]
    ): Property = for {
      fa  <- genFA.log("fa")
      amb <- genAMB.log("amb")
      anb <- genANB.log("anb")
    } yield {
      Result.assert(F.traverseLaw.parallelFusion[N, M, A, B](fa, amb, anb))
    }


  }
}
