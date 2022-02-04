package pirate
package internal

import scalaz._, Scalaz._

import hedgehog.runner._

import pirate.spec.Laws._
import pirate.spec.Gens

object NondetTSpec extends Properties {

  override def tests: List[Prop] =
    equal.laws[NondetT[Identity, Int]](
        "NondetT is an equal (for tests)", Gens.genNondetT(Gens.genInt)
      ) ++
      monad.laws[NondetX](
          "NondetT is a monad"
        , Gens.genInt
        , Gens.genIntToInt
        , Gens.genNondetT(Gens.genInt)
        , Gens.genIntToInt.map(f => (n: Int) => NondetT.singleton(f(n)))
        , Gens.genIntToInt.map(f => NondetT.singleton(f))
      ) ++
    monadPlus.laws[NondetX](
        "NondetT is a monad plus"
      , Gens.genInt
      , Gens.genIntToInt
      , Gens.genNondetT(Gens.genInt)
      , Gens.genIntToInt.map(f => (n: Int) => NondetT.singleton(f(n)))
      , Gens.genIntToInt.map(f => NondetT.singleton(f))
      ) ++
    monadPlus.strongLaws[NondetX](
      "NondetT is a strong monad plus"
      , Gens.genInt
      , Gens.genIntToInt
      , Gens.genNondetT(Gens.genInt)
      , Gens.genIntToInt.map(f => (n: Int) => NondetT.singleton(f(n)))
      , Gens.genIntToInt.map(f => NondetT.singleton(f))
      )

  type NondetX[A] = NondetT[Identity, A]

  /* testing only instances */
  implicit def NondetTEqual[F[_], A](implicit E: Equal[F[(Boolean, List[A])]], F: Monad[F]): Equal[NondetT[F, A]] =
    Equal.equal[NondetT[F, A]]((a, b) => a.runNondetT.run.run(true) === b.runNondetT.run.run(true) && a.runNondetT.run.run(false) === b.runNondetT.run.run(false))

}
