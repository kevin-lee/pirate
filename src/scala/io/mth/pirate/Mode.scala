package io.mth.pirate

sealed trait Mode[A, B] {
  def fold[X](
    r: (Flags[A], Positionals[A], A => B) => X,
    s: (Flags[A], Command[A, B]) => X
  ): X = this match {
    case RunnableMode(flags, positionals, f) => r(flags, positionals, f)
    case SuperMode(flags, command) => s(flags, command)
  }

  /**
   * Combine this command with a new flag, and return the
   * the new command. The operation to combine flags is
   * associative, i.e. not order dependant.
   */
  def <|>(flag: Flag[A]): Mode[A, B] = fold(
    (flags, pos, f) => RunnableMode(flags <|> flag, pos, f),
    (flags, command) => SuperMode(flags <|> flag, command)
  )

  /**
   * Combine this command with a new positional parameters,
   * and return the new command. The operation to combine
   * positional parameters is NOT associative. Parsing and
   * generating a usage string is heavily dependent on the
   * order in which positional parameters added to the
   * command.
   */
  def >|(positional: Positional[A]): Mode[A, B] = fold(
    (flags, pos, f) => RunnableMode(flags, pos >| positional, f),
    (flags, command) => this
  )
}

private case class RunnableMode[A, B](flags: Flags[A], positionals: Positionals[A], f: A => B) extends Mode[A, B]
private case class SuperMode[A, B](flags: Flags[A], command: Command[A, B]) extends Mode[A, B]
