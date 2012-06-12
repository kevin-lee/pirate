package io.mth.pirate

import Update._

sealed trait Occurrence
case object Once extends Occurrence
case object MaybeOnce extends Occurrence
case object Many
case object MaybeMany extends Occurrence

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
    s: (List[Decl], String, Occurrence, Update[A, Unit]) => X,
    m: (List[Decl], String, String, Occurrence, Update[A, String]) => X,
    o: (List[Decl], String, String, Occurrence, Update[A, Option[String]]) => X
  ): X = this match {
    case Switch(decls, description, occ, update) => s(decls, description, occ, update)
    case FlagArg(decls, meta, description, occ, update) => m(decls, meta, description, occ, update)
    case FlagOptArg(decls, meta, description, occ, update) => o(decls, meta, description, occ, update)
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
    (_, d, _, _) => d,
    (_, _, d, _, _) => d,
    (_, _, d, _, _) => d
  )

  def declarations = fold(
    (x, _, _, _) => x,
    (x, _, _, _, _) => x,
    (x, _, _, _, _) => x
  )

}

private case class Switch[A](decls: List[Decl], desc: String, occurrence: Occurrence, update: Update[A, Unit]) extends Flag[A]
private case class FlagArg[A](decls: List[Decl], meta: String, desc: String, occurrence: Occurrence, update: Update[A, String]) extends Flag[A]
private case class FlagOptArg[A](decls: List[Decl], meta: String, desc: String, occurrence: Occurrence, update: Update[A, Option[String]]) extends Flag[A]

object Flag {
  /**
   *  Type constructor for a Flag with only a short identifier, and no argument.
   */
  def short[A](short: Char, desc: String)(f: A => A): Flag[A] =
    Switch(Decl.short(short) :: Nil, desc, MaybeMany, (a, _) => Right(f(a)))

  /**
   * Type constructor for a Flag with only a long identifier, and no argument.
   */
  def long[A](long: String, desc: String)(f: A => A): Flag[A] =
    Switch(Decl.long(long) :: Nil, desc, MaybeMany, (a, _) => Right(f(a)))

  /**
   * Type constructor for a Flag with both a short and long identifier, and no argument.
   */
  def flag[A](short: Char, long: String, desc: String)(f: A => A): Flag[A] =
    Switch(Decl.short(short) :: Decl.long(long) :: Nil, desc, MaybeMany, (a, _) => Right(f(a)))

  /**
   * Type constructor for a Flag with only a short identifier, and with an argument.
   */
  def short1[A](short: Char, desc: String, meta: String)(f: (A, String) => A): Flag[A] =
    FlagArg(Decl.short(short) :: Nil, meta, desc, MaybeMany, (a, s) => Right(f(a, s)))

  /**
   * Type constructor for a Flag with only a long identifier, and with an argument.
   */
  def long1[A](long: String, desc: String, meta: String)(f: (A, String) => A): Flag[A] =
    FlagArg(Decl.long(long) :: Nil, meta, desc, MaybeMany, (a, s) => Right(f(a, s)))

  /**
   * Type constructor for a Flag with both a long and short identifier, and with an argument.
   */
  def flag1[A](short: Char, long: String, desc: String, meta: String)(f: (A, String) => A): Flag[A] =
    FlagArg(Decl.short(short) :: Decl.long(long) :: Nil, meta, desc, MaybeMany, (a, s) => Right(f(a, s)))
}

