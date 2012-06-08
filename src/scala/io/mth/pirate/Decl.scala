package io.mth.pirate

sealed trait Decl {
  def fold[X](
    short: Char => X,
    long: String => X
  ): X = this match {
    case Short(c) => short(c)
    case Long(s) => long(s)
  }
}

private case class Short(c: Char) extends Decl
private case class Long(s: String) extends Decl

object Decl {
  def short(c: Char): Decl =
    Short(c)

  def long(s: String): Decl =
    Long(s)
}
