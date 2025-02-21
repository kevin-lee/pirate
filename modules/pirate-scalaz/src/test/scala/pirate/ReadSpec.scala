package pirate

import java.io.File
import java.net.{URI, URL}

import scalaz._, Scalaz._

import hedgehog._
import hedgehog.runner.{example => exampleTest, _}

import pirate.spec.Gens

object ReadSpec extends Properties {

  override def tests: List[Test] = List(
    property("Char", symmetric(Gen.unicode)),
    property("String", symmetric(Gens.genUnicodeString(0, 50))),
    property("Short", symmetric(Gen.short(Range.linear(0, 10)))),
    property("Int", symmetric(Gen.int(Range.linear(Int.MinValue, Int.MaxValue)))),
    property("Long", symmetric(Gen.long(Range.linear(Long.MinValue, Long.MaxValue)))),
    property("Double", symmetric(Gen.double(Range.linearFrac(Double.MinValue, Double.MaxValue)))),
    property("Boolean", symmetric(Gen.boolean)),
    property("BigInt", symmetric(Gen.long(Range.linear(Long.MinValue, Long.MaxValue)).map(BigInt.apply))),
    exampleTest("File example", file),
    exampleTest("URI example", uri),
    exampleTest("URL example", url),
    property("Char doesn't parse strings", charerr),
    property("Numeric doesn't parse strings", numericerr),
    property("Returning none will fail", optionFail),
    property("Returning some will pass", optionPass),
    property("Returning left will fail", eitherFail),
    property("Returning right will pass", eitherPass),
    exampleTest("Witness (compilation is sufficient)", Result.success)
  )

  def file: Result =
    Read.parse[File](List("some/file")).toOption ==== Some(new File("some/file"))

  def uri: Result =
    Read.parse[URI](List("http://some/file")).toOption ==== Some(new URI("http://some/file"))

  def url: Result =
//    Read.parse[URL](List("http://some/file")).toOption ==== Some(new URL("http://some/file"))
    Read.parse[URL](List("http://some/file")).toOption ==== Some(java.net.URI.create("http://some/file").toURL)

  def charerr: Property = for {
    c <- Gen.unicode.log("c")
    d <- Gen.unicode.log("d")
    s <- Gens.genUnicodeString(0, 50).log("s")
  } yield {
    Read.parse[Char](List(c.toString + d.toString + s)).toOption ==== None
  }

  def numericerr: Property = for {
    s <- Gens.genUnicodeString(0, 50).filter(!_.parseInt.isSuccess).log("s")
  } yield {
    Read.parse[Int](List(s)).toOption ==== None
  }

  def optionFail: Property = for {
    s <- Gens.genUnicodeString(0, 50).log("s")
    e <- Gens.genUnicodeString(0, 50).log("e")
  } yield {
    Read.optionRead(_ => None, e).read(List(s)) ==== ReadErrorInvalidType(s, e).left
  }

  def optionPass: Property = for {
    s <- Gens.genUnicodeString(0, 50).log("s")
    r <- Gens.genUnicodeString(0, 50).list(Range.linear(0, 20)).log("r")
    e <- Gens.genUnicodeString(0, 50).log("e")
  } yield {
    Read.optionRead(Some(_), e).read(s :: r) ==== (r, s).right
  }

  def eitherFail: Property = for {
    s <- Gens.genUnicodeString(0, 50).log("s")
    e <- Gens.genUnicodeString(0, 50).log("e")
  } yield {
    Read.eitherRead(_ => e.left).read(List(s)) ==== ReadErrorInvalidType(s, e).left
  }

  def eitherPass: Property = for {
    s <- Gens.genUnicodeString(0, 50).log("s")
    r <- Gens.genUnicodeString(0, 50).list(Range.linear(0, 20)).log("r")
  } yield {
    Read.eitherRead(_.right).read(s :: r) ==== (r, s).right
  }

  def symmetric[A: Read](genA: Gen[A]): Property = for {
    a <- genA.log("a")
  } yield {
    Read.parse[A](List(a.toString)).toOption ==== Some(a)
  }

  Read.of[Char]
  Read.of[String]
  Read.of[Short]
  Read.of[Int]
  Read.of[Long]
  Read.of[Double]
  Read.of[Boolean]
  Read.of[BigInt]
  Read.of[java.io.File]
  Read.of[java.net.URI]
  Read.of[java.net.URL]
  Read.of[Option[String]]
  Read.of[(Int, Int)]
  Read.of[(String, Int)]
  Read.of[(Int, String)]
  Read.of[(Int, Int, Int, Int, Int)]
  Read.of[(Int, Option[Int])]

}
