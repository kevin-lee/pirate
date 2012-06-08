package io.mth.pirate

import java.io.PrintStream


/**
 * Command data type. Represents a command name; a possible
 * description; a set of flags; and positional parameters.
 *
 * The data type includes combinators for building up more
 * complex commands from a simple command name.
 *
 * TODO: multi-form and multi-modal commands.
 */
sealed trait Command[A, B] {
  import scalaz._
  import Command._

  val name: String
  val description: String
  val modes: List[Mode[A, B]]

  /**
   * Combine this command with the specified description and
   * return the new description. If a description is already
   * set it shall be replaced.
   */
  def ~(description: String): Command[A, B] =
    commandline(name, description, modes)

  /**
   * The usage string for this command using the default
   * usage mode. Equivalent to usageForMode(DefaultUsageMode).
   */
  def usage = usageForMode(DefaultUsageMode)

  /**
   * The usage string for this command using the specified
   * usage mode.
   */
  def usageForMode(mode: UsageMode) = Usage.usage(mode)(this)

  /**
   * Create an argument parser for this command. This is for advanced
   * usage only. It is expected that the `parse` method is sufficient
   * for most cases.
   */
  def toParser: Parser[A => A] =
    sys.error("todo")

  /**
   * Parse a list of arguments based on this command and apply the resultant
   * function to the data object.
   */
  def parse(args: List[String], default: A): Validation[String, A] =
    toParser.parse(args) match {
      case Success((rest, f)) =>
        if (rest.isEmpty) Success(f(default))
        else Failure("Too many arguments / Arguments could not be parsed: " + rest)
      case Failure(msg) => Failure(msg)
    }

  /**
   * Higher order function to handle parse and dispatch. This is
   * a convenience only.
   */
  def dispatchOrUsage(args: List[String], default: A, err: PrintStream = System.err)(f: A => Unit): Int =
    dispatch(args, default)(f)(msg => err.println(msg + "\n\n" + usage))

  /**
   * Higher order function to handle parse and dispatch. This is
   * a convenience only.
   */
  def dispatchOrDie(args: List[String], default: A, err: PrintStream = System.err)(f: A => Unit): Unit =
    sys.exit(dispatchOrUsage(args, default)(f))

  /**
   * Higher order function to handle parse and dispatch. This is
   * a convenience only.
   */
  def dispatch(args: List[String], default: A)(success: A => Unit)(error: String => Unit): Int =
    parse(args, default) match {
      case Success(applied) => success(applied); 0
      case Failure(msg) => error(msg); 1
    }
}

object Command {
  /**
   *  Type constructor for the most basic command. It is recommended that this
   * constructor is used with the combinators on Command to build up a command.
   *
   * It is also recommended to explicitly specify a type parameter when using
   * this constructor. This will greatly aid the scala type inference algorithm
   * when applying the combinators. This means use command[ArgType]("name"),
   * rather than command("name").
   */
  def command[A, B](name: String): Command[A, B] =
    commandline(name, "", Nil)

  /**
   * Type constructor for a complete command. This is for advanced usage only.
   * The equivalent commands can be built using the `command` constructor
   * and the combinators.
   */
  def commandline[A, B](cmdname: String, cmddescription: String, cmdmodes: List[Mode[A, B]]): Command[A, B] =
    new Command[A, B] {
      val name = cmdname
      val description = cmddescription
      val modes = cmdmodes
    }
}
