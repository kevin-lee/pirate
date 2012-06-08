package io.mth.pirate

sealed trait Arity {
  def fold[X](
    fixed: Int => X,
    variable: Int => X
  ): X = this match {
    case Fixed(n) => fixed(n)
    case Variable(min) => variable(min)
  }
}

private case class Fixed(n: Int) extends Arity
private case class Variable(min: Int) extends Arity

object Arity {
  def fixed(n: Int): Arity =
    Fixed(n)

  def variable(min: Int): Arity =
    Variable(min)

}


