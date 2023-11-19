package pirate.spec

import hedgehog._

import pirate._
import pirate.internal._

import scalaz.Scalaz._
import scalaz.{ListT => _, Name => _, _}

object Gens {

  def genInt: Gen[Int] = Gen.int(Range.linear(Int.MinValue, Int.MaxValue))

  def genIntToInt: Gen[Int => Int] =
    /* The current Hedgehog does not have combinators for function in Gen
     * so Int => Int functions are hard-coded for now.
     * Feature request: https://github.com/hedgehogqa/scala-hedgehog/issues/90
     */
    Gen.element1[Int => Int](
      identity[Int],
      x => x + x,
      x => x - x,
      x => x * x,
      x => x + 100,
      x => x * 100,
      x => x - 100
    )

  def genUnicodeString(min: Int, max: Int): Gen[String] =
    Gen.string(Gen.unicode, Range.linear(min, max))

  case class SmallInt(value: Int)

  def genSmallInt: Gen[SmallInt] =
    Gen.int(Range.linear(0, 1000)).map(SmallInt.apply)

  case class LongLine(value: String)

  case class List5[A](value: List[A])
  def genList5[A](genA: Gen[A]): Gen[List5[A]] =
    for {
      n <- Gen.int(Range.linear(0, 5))
      l <- genA.list(Range.singleton(n))
    } yield List5(l)

  def genLongLine: Gen[LongLine] =
    Gen.string(Gen.unicode, Range.singleton(1000)).map(LongLine.apply)

  def genName: Gen[Name] =
    Gen.choice1(
      Gen.alpha.map(ShortName.apply),
      Gen.string(Gen.alpha, Range.linear(0, 20)).map(LongName.apply),
      for { c <- Gen.alpha; s <- Gen.string(Gen.alpha, Range.linear(0, 20)) } yield Name(c, s)
    )

  def genTStep[A, X](genA: Gen[A], genX: Gen[X]): Gen[TStep[A, X]] =
    Gen.choice1(
      Gen.elementUnsafe(List(TNil[A, X]())),
      for { a <- genA; x <- genX } yield TCons(a, x)
    )

  def genListT[A](genA: Gen[A]): Gen[ListT[Identity, A]] =
    for {
      n <- Gen.int(Range.linear(0, 10))
      x <- genA.list(Range.singleton(n))
    } yield x.foldRight(ListT.nil[Identity, A])(ListT.cons[Identity, A])

  def genNondetT[A](genA: Gen[A]): Gen[NondetT[Identity, A]] = {
    type S[B] = StateT[Identity, Boolean, B]
    for {
      n <- Gen.int(Range.linear(0, 10))
      x <- genA.list(Range.singleton(n))
    } yield NondetT(x.foldRight(ListT.nil[S, A])(ListT.cons[S, A]))
  }

  def genParse[A: Read](genA: Gen[A]): Gen[Parse[A]] =
    // TODO: make it lazy
    Gen.choice1(
      genA.map(a => ValueParse(a)),
      genParser(genA).map(p => ParserParse(p)),
      for {
        a <- genParse(genA)
        b <- genParse(genA)
      } yield AltParse(a, b)
    )

  def genParser[A: Read](genA: Gen[A]): Gen[Parser[A]] =
    Gen.choice1(
      genSwitchParser(genA),
      genFlagParser,
      genArgumentParser
    )

  def genSwitchParser[A](genA: Gen[A]): Gen[Parser[A]] =
    for {
      n    <- genName
      desc <- genUnicodeString(0, 20).option
      mvar <- genUnicodeString(0, 10).option
      hid  <- Gen.boolean
      a    <- genA
    } yield SwitchParser[A](n, Metadata(desc, mvar, hid), a)

  def genFlagParser[A: Read]: Gen[Parser[A]] =
    for {
      n    <- genName
      desc <- genUnicodeString(0, 20).option
      mvar <- genUnicodeString(0, 10).option
      hid  <- Gen.boolean
    } yield FlagParser[A](n, Metadata(desc, mvar, hid), Read.of[A])

  def genArgumentParser[A: Read]: Gen[Parser[A]] =
    for {
      desc <- genUnicodeString(0, 20).option
      mvar <- genUnicodeString(0, 10).option
      hid  <- Gen.boolean
    } yield ArgumentParser[A](Metadata(desc, mvar, hid), Read.of[A])
}
