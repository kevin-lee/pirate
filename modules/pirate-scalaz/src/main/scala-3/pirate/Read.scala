package pirate

import scalaz._
import Scalaz._

import scala.util.Try

/** Minimal parser combinator library.
  */
case class Read[A](read: List[String] => ReadError \/ (List[String], A)) {

  def map[B](f: A => B): Read[B] =
    flatMap(f andThen (Read.value(_)))

  def flatMap[B](f: A => Read[B]): Read[B] =
    Read(in =>
      read(in) match {
        case -\/(e) => -\/(e)
        case \/-((rest, a)) => f(a).read(rest)
      }
    )

  /** Choice combinator, this or that (if this fails).
    */
  def |||(that: => Read[A]): Read[A] =
    Read(in =>
      read(in) match {
        case -\/(_) => that.read(in)
        case r @ \/-(_) => r
      }
    )

  /** List combinator, zero or more times.
    */
  def list: Read[List[A]] =
    list1 ||| Read.value(List[A]())

  /** List combinator, one or more times.
    */
  def list1: Read[List[A]] =
    lift2(list)(_ :: _)

  /** Symbolic representation of list combinator, zero or more times.
    *
    * See: list.
    */
  def * = list

  /** Symbolic representation of list combinator, one or more times.
    *
    * See list1.
    */
  def + = list1

  /** Lazy implementation of lift 2.
    */
  def lift2[B, C](p: => Read[B])(f: (A, B) => C): Read[C] =
    flatMap((a: A) => p.map(f(a, _)))

  def option: Read[Option[A]] =
    map(Option.apply) ||| Read.value[Option[A]](None)
}

object Read {
  def of[A: Read]: Read[A] =
    summon[Read[A]]

  def read[A: Read](in: List[String]): ReadError \/ (List[String], A) =
    of[A].read(in)

  def parse[A: Read](in: List[String]): ReadError \/ A =
    read[A](in) flatMap ({
      case (Nil, a) => a.right
      case (_ :: _, _) => ReadErrorTooMuchInput.left
    })

  /** The parser that always succeeds.
    */
  def value[A](v: => A): Read[A] =
    Read(in => (in, v).right)

  /** The parser that always fails with message.
    */
  def failure[A](message: String): Read[A] =
    error(ReadErrorMessage(message))

  /** The parser that always fails.
    */
  def error[A](err: ReadError): Read[A] =
    Read(_ => err.left)

  /** String parser, consumes a single string off the input, fails
    * on empty input.
    */
  def string: Read[String] =
    Read(in =>
      in match {
        case Nil => ReadErrorNotEnoughInput.left
        case h :: t => (t, h).right
      }
    )

  /** Consumes a string off the input, iff pred is true for that
    * string.
    */
  def satisfy(pred: String => Boolean): Read[String] =
    string >>=
      (s => if (pred(s)) value(s) else error(ReadErrorUnexpected(s)))

  /** Consumes a string off the input, iff it equals string s.
    */
  def is(s: String): Read[String] =
    satisfy(_ == s)

  /** Multi-way choice, first success proceeds.
    */
  def choiceN[A](ps: List[Read[A]]): Read[A] =
    ps.foldLeft(error[A](ReadErrorEmpty))(_ ||| _)

  def tryRead[A](f: String => A, expected: String): Read[A] =
    optionRead(s => Try(f(s)).toOption, expected)

  def optionRead[A](f: String => Option[A], expected: String): Read[A] =
    eitherRead(s => f(s).toRightDisjunction(expected))

  def eitherRead[A](f: String => String \/ A): Read[A] =
    string.flatMap(s => f(s).fold(e => error(ReadErrorInvalidType(s, e)), value(_)))

  given ReadChar: Read[Char] =
    optionRead(s => (s.length == 1).option(s.charAt(0)), "Char")

  given ReadString: Read[String] =
    string

  given ReadShort: Read[Short] =
    tryRead(_.toShort, "Short")

  given ReadInt: Read[Int] =
    tryRead(_.toInt, "Int")

  given ReadLong: Read[Long] =
    tryRead(_.toLong, "Long")

  given ReadDouble: Read[Double] =
    tryRead(_.toDouble, "Double")

  given ReadBoolean: Read[Boolean] =
    tryRead(_.toBoolean, "Boolean")

  given ReadBigInt: Read[BigInt] =
    tryRead(BigInt(_), "BigInt")

  given ReadFile: Read[java.io.File] =
    string map (new java.io.File(_))

  given ReadURI: Read[java.net.URI] =
    tryRead(new java.net.URI(_), "URI")

  given ReadURL: Read[java.net.URL] =
    tryRead(new java.net.URI(_).toURL, "URL")

  given ReadOption[A: Read]: Read[Option[A]] =
    of[A].option

  given ReadMonad: Monad[Read] with MonadPlus[Read] with {
    def point[A](a: => A)                       = value(a)
    def bind[A, B](p: Read[A])(f: A => Read[B]) = p flatMap f
    def empty[A]                                = error[A](ReadErrorEmpty)
    def plus[A](p1: Read[A], p2: => Read[A])    = p1 ||| p2
  }

  given ReadMonoid[A]: Monoid[Read[A]] with {
    def zero                                         = error(ReadErrorEmpty)
    def append(p1: Read[A], p2: => Read[A]): Read[A] = p1 ||| p2
  }

  given readEmptyTuple: Read[EmptyTuple] = Read.value(EmptyTuple)

  given readTuple[A: Read, T <: Tuple: Read]: Read[A *: T] = for {
    a <- summon[Read[A]]
    t <- summon[Read[T]]
  } yield a *: t

}

sealed trait ReadError
case object ReadErrorEmpty extends ReadError
case object ReadErrorNotEnoughInput extends ReadError
case object ReadErrorTooMuchInput extends ReadError
case class ShowHelpText(sub: Option[String]) extends ReadError
case class ShowOkText(s: String) extends ReadError
case class ReadErrorInvalidType(token: String, expected: String) extends ReadError
case class ReadErrorUnexpected(token: String) extends ReadError
case class ReadErrorMessage(message: String) extends ReadError
