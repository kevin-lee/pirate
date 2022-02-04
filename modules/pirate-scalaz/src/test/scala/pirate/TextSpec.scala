package pirate

import hedgehog._
import hedgehog.runner._

import pirate.spec.Gens

object TextSpec extends Properties {

  override def tests: List[Test] = List(
    property("all spaces", spaces),
    property("correct length", length),
    property("wrap no longer than width", width),
    property("wrap no longer than width + indent", indent),
    property("handle negative widths", negativeWidth),
    property("never lose content", safe),
    property("text with new lines has proper gutter", gutter)
  )

  import Text._

  def spaces: Property = for {
    n <- Gens.genSmallInt.log("n")
  } yield {
    Result.assert(space(n.value).forall(_ == ' '))
  }

  def length: Property = for {
    n <- Gens.genSmallInt.log("n")
  } yield {
    space(n.value).length ==== n.value
  }

  def width: Property = for {
    l <- Gens.genLongLine.log("l")
  } yield {
    Result.assert(
      wrap("", 0)(l.value, 80, 0)
        .split('\n')
        .forall(_.length <= 80)
    )
  }

  def indent: Property = for {
    l <- Gens.genLongLine.log("l")
  } yield {
    Result.assert(
      wrap("", 0)(l.value, 80, 10)
        .split('\n')
        .forall(_.length <= 90)
    )
  }

  def negativeWidth: Property = for {
    l <- Gens.genLongLine.log("l")
    s <- Gens.genSmallInt.log("s")
  } yield {
    Result.assert(
      wrap("", 0)(l.value, 50 - s.value, 10)
        .split('\n')
        .forall(_.length <= 90)
    )
  }

  def safe: Property = for {
    l <- Gens.genLongLine.log("l")
  } yield {
    Result.assert(drains(l.value.trim, wrap("", 0)(l.value, 80, 5)))
  }

  def gutter: Property = for {
    ls <- Gens.genList5(Gens.genLongLine).log("ls")
  } yield {
    wrap("", 0)(ls.value.map(_.value).mkString("\n"), 80, 10)
      .split('\n')
      .map(_.take(10))
      .mkString("")
      .trim ==== ""
  }

  def drains(orig: String, modded: String): Boolean = {
    var i = 0
    modded.foreach(c =>
      if (i < orig.length && (orig.charAt(i) == c || (orig.charAt(i) == ' ' && c == '\n')))
        i += 1
    )
    orig.length == i
  }
}
