package pirate

import scalaz.{Name => _, _}

sealed trait Parser[A] {
  def map[B](f: A => B): Parser[B] = this match {
    case SwitchParser(flag, meta, a) =>
      SwitchParser(flag, meta, f(a))
    case FlagParser(flag, meta, p) =>
      FlagParser(flag, meta, p.map(f))
    case ArgumentParser(meta, p) =>
      ArgumentParser(meta, p.map(f))
    case CommandParser(sub) =>
      CommandParser(sub.copy(parse = sub.parse.map(f)))
  }

  def isArg: Boolean = this match {
    case ArgumentParser(_, _) => true
    case _                    => false
  }
  def isVisible: Boolean = this match {
    case SwitchParser(_, meta, _) => meta.visible
    case FlagParser(_, meta, _) => meta.visible
    case ArgumentParser(meta, _) => meta.visible
    case CommandParser(sub) => true
  }
}

case class SwitchParser[A](flag: Name, meta: Metadata, a: A) extends Parser[A]
case class FlagParser[A](flag: Name, meta: Metadata, p: Read[A]) extends Parser[A]
case class ArgumentParser[A](meta: Metadata, p: Read[A]) extends Parser[A]
case class CommandParser[A](sub: Command[A]) extends Parser[A]

object Parser {
  implicit def ParserFunctor: Functor[Parser] = new Functor[Parser] {
    def map[A, B](a: Parser[A])(f: A => B): Parser[B] = a map f
  }

}
