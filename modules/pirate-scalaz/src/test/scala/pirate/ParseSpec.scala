package pirate

import hedgehog._
import hedgehog.runner._

import pirate.spec.Gens

object ParseSpec extends Properties {

  override def tests: List[Test] = List(
    property("Verify basic name pass-through to command", name),
    property("Verify description not set on command", desc),
    property("Verify description can be set on command", descSet),
    property("Verify name and description can be set on command", nameAndDescSet)
  )

  def name: Property = for {
    n <- Gens.genUnicodeString(0, 50).log("n")
  } yield {
    (ValueParse(None) ~ n).name ==== n
  }

  def desc: Property = for {
    n <- Gens.genUnicodeString(0, 50).log("n")
  } yield {
    (ValueParse(None) ~ n).description ==== None
  }

  def descSet: Property = for {
    n1 <- Gens.genUnicodeString(0, 50).log("n1")
    n2 <- Gens.genUnicodeString(0, 50).log("n2")
  } yield {
    (ValueParse(None) ~ n1 ~~ n2).description ==== Some(n2)
  }

  def nameAndDescSet: Property = for {
    n1 <- Gens.genUnicodeString(0, 50).log("n1")
    n2 <- Gens.genUnicodeString(0, 50).log("n2")
  } yield {
    val parse = ValueParse(None) ~ n1 ~~ n2
    parse.name ==== n1 and parse.description ==== Some(n2)
  }
}
