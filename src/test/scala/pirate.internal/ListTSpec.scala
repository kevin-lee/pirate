package pirate
package internal

import scalaz._, Scalaz._

import hedgehog.runner._

import pirate.spec.Gens
import pirate.spec.Laws._

object ListTSpec extends Properties {
  override def tests: List[Test] =
    equal.laws[TStep[Int, Int]]("TStep is an equal", Gens.genTStep(Gens.genInt, Gens.genInt)) ++
      bifunctor.laws[TStep](
          "TStep is a bifunctor"
        , Gens.genInt.flatMap(n1 => Gens.genInt.map(n2 => TStep.cons(n1, n2))), Gens.genIntToInt
        ) ++
      equal.laws[ListT[Identity, Int]]("ListT is an equal (for tests)", Gens.genListT(Gens.genInt)) ++
      monad.laws[ListX](
          "ListT is a monad"
        , Gens.genInt
        , Gens.genIntToInt
        , Gens.genListT(Gens.genInt)
        , Gens.genIntToInt.map(f => (n: Int) => ListT.singleton(f(n)))
        , Gens.genIntToInt.map(f => ListT.singleton(f))
        ) ++
      monadPlus.laws[ListX](
          "ListT is a monad plus"
        , Gens.genInt
        , Gens.genIntToInt
        , Gens.genListT(Gens.genInt)
        , Gens.genIntToInt.map(f => (n: Int) => ListT.singleton(f(n)))
        , Gens.genIntToInt.map(f => ListT.singleton(f))
      ) ++
      monadPlus.strongLaws[ListX](
          "ListT is a strong monad plus"
        , Gens.genInt
        , Gens.genIntToInt
        , Gens.genListT(Gens.genInt)
        , Gens.genIntToInt.map(f => (n: Int) => ListT.singleton(f(n)))
        , Gens.genIntToInt.map(f => ListT.singleton(f))
      )

  type ListX[A] = ListT[Identity, A]

  /* testing only instances */
  implicit def ListTEqual[F[+_], A](implicit E: Equal[F[List[A]]], F: Monad[F]): Equal[ListT[F, A]] =
    Equal.equal[ListT[F, A]]((a, b) => a.run === b.run)
}
