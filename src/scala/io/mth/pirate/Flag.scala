package io.mth.pirate

import Update._

/**
 * Flag data type. Can represent one of:
 *  - flag with short and long identifier, and no argument
 *  - flag with only short identifier, and no argument
 *  - flag with only long identifier, and no argument
 *  - flag with short and long identifier, and an argument
 *  - flag with only short identifier, and an argument
 *  - flag with only long identifier, and an argument
 *
 * Each of the variants of this type include a function
 * for transforming a type if it is succeeds in parsing.
 */
sealed trait Flag[A] {
  import scalaz._
  import Scalaz._
  import Parser._
  import Flag._

  /**
   * Catamorphism for the Flag data type.
   */
  def fold[X](
    s: (List[Decl], String, Update[A, Unit]) => X,
    m: (List[Decl], String, String, Update[A, String]) => X,
    o: (List[Decl], String, String, Update[A, Option[String]]) => X
  ): X = this match {
    case Switch(decls, description, update) => s(decls, description, update)
    case MandatoryFlag(decls, meta, description, update) => m(decls, meta, description, update)
    case OptionalFlag(decls, meta, description, update) => o(decls, meta, description, update)
  }

  /**
   * Combine this flag with a new flag, and return as
   * a new flags. The operation to combine flags is
   * associative, i.e. not order dependant.
   */
  def <|>(flag: Flag[A]) = toFlags <|> flag

  /**
   * Convert this Flag into a Flags.
   */
  def toFlags = Flags.flags(this)

  /**
   * The description for this flag.
   */
  def description = fold(
    (_, d, _) => d,
    (_, _, d, _) => d,
    (_, _, d, _) => d
  )
}

private case class Switch[A](decls: List[Decl], desc: String, update: Update[A, Unit]) extends Flag[A]
private case class MandatoryFlag[A](decls: List[Decl], meta: String, desc: String, update: Update[A, String]) extends Flag[A]
private case class OptionalFlag[A](decls: List[Decl], meta: String, desc: String, update: Update[A, Option[String]]) extends Flag[A]

object Flag {
  /**
   *  Type constructor for a Flag with only a short identifier, and no argument.
   */
  def short[A](short: Char, desc: String)(f: A => A): Flag[A] =
    Switch(Decl.short(short) :: Nil, desc, (a, _) => Right(f(a)))

  /**
   * Type constructor for a Flag with only a long identifier, and no argument.
   */
  def long[A](long: String, desc: String)(f: A => A): Flag[A] =
    Switch(Decl.long(long) :: Nil, desc, (a, _) => Right(f(a)))

  /**
   * Type constructor for a Flag with both a short and long identifier, and no argument.
   */
  def flag[A](short: Char, long: String, desc: String)(f: A => A): Flag[A] =
    Switch(Decl.short(short) :: Decl.long(long) :: Nil, desc, (a, _) => Right(f(a)))

  /**
   * Type constructor for a Flag with only a short identifier, and with an argument.
   */
  def short1[A](short: Char, desc: String, meta: String)(f: (A, String) => A): Flag[A] =
    MandatoryFlag(Decl.short(short) :: Nil, meta, desc, (a, s) => Right(f(a, s)))

  /**
   * Type constructor for a Flag with only a long identifier, and with an argument.
   */
  def long1[A](long: String, desc: String, meta: String)(f: (A, String) => A): Flag[A] =
    MandatoryFlag(Decl.long(long) :: Nil, meta, desc, (a, s) => Right(f(a, s)))

  /**
   * Type constructor for a Flag with both a long and short identifier, and with an argument.
   */
  def flag1[A](short: Char, long: String, desc: String, meta: String)(f: (A, String) => A): Flag[A] =
    MandatoryFlag(Decl.short(short) :: Decl.long(long) :: Nil, meta, desc, (a, s) => Right(f(a, s)))
}

