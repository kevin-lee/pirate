package pirate

import Pirate._
import pirate.internal._
import scalaz._, Scalaz._

import hedgehog._
import hedgehog.runner.{example => exampleTest, _}

import pirate.spec.Gens

sealed trait TestCommand
case class TestWrapper(cmd: TestCommand)
case object TestA extends TestCommand
case object TestB extends TestCommand

object InterpreterSpec extends Properties {

  override  def tests: List[Test] = List(
    /* Interpreter Properties */
      exampleTest("Required found", requiredFound)
    , exampleTest("Required missing applicatives both", requiredMissingA)
    , exampleTest("Required missing applicatives first", requiredMissingB)
    , exampleTest("Required missing applicatives second", requiredMissingC)
    , exampleTest("Left over arguments fails", leftover)
    , exampleTest("Two alternatives missing", requiredMissingAlts)
    , exampleTest("Default found", defaultFound)
    , exampleTest("Default missing", defaultMissing)
    , exampleTest("Option found", optionFound)
    , exampleTest("Option missing", optionMissing)
    , property("Assignment found", assignmentFound)
    , property("Assignment missing", assignmentMissing)
    , exampleTest("Switches toggle on", switchesOn)
    , exampleTest("Switches are off unless toggled", switchesOff)
    , exampleTest("Multiple switches work in a single entry", multipleSwitches)
    , exampleTest("Short option flag can come at the end of switch", flagAfterSwitch)
    , exampleTest("Can measure the length of a set of flags", flagsLength)
    , exampleTest("Short option flag args works without spaces", shortFlagPost)
    , exampleTest("Position arguments work", positionalArgs)
    , property("Many arguments work", manyArgs)
    , property("Many arguments work after a positional", positionalFollowingMany)
    , property("Many arguments work before a positional", manyFollowingPositional)
    , exampleTest("Some fails on empty", someFailsOnEmpty)
    , exampleTest("Invalid options produces reasonable error", invalidOpt)
    , exampleTest("Invalid argument produces reasonable error", invalidArg)
    , exampleTest("Arguments which parse poorly produces reasonable error", intArgString)
    , exampleTest("Missing arguments produce sane errors", missingArg)

    /* Composite interpreters */
    , exampleTest("Backtracking occurs when set", dobacktrack)
    , exampleTest("Backtracking does not occurs when set", donotbacktrack)
    , exampleTest("Interpreter still works with backtracking on", donotbacktrackbutstillwork)
    , property("Interpreter handles the first multiple cases", orFirst)
    , property("Interpreter handles the second multiple cases", orSecond)
    , property("Interpreter handles wrapped commands well", wrappers)
    , exampleTest("Context flows through multiple subcommands", subcontext)
    )

  def run[A](p: Parse[A], args: List[String]): ParseError \/ A = Interpreter.run(p, args, DefaultPrefs())._2

  def testA(name: String): Parse[TestCommand] =
    terminator(long(name), Flags.empty, TestA)

  def testB(name: String): Parse[TestCommand] =
    terminator(long(name), Flags.empty, TestB)

  def wrap(cmd: Parse[TestCommand]): Parse[TestWrapper] =
    cmd.map(TestWrapper)

  def requiredFound: Result =
    run(flag[String](short('a'), Flags.empty), List("-a", "b")) ==== "b".right

  def requiredMissingA: Result =
    run((flag[String](short('a'), Flags.empty) |@| flag[String](short('b'), Flags.empty))(_ -> _), List()).toEither ==== Left(ParseErrorMissing(ParseTreeAp(List(ParseTreeLeaf(FlagInfo(ShortName('a'), None, None, false, false)), ParseTreeLeaf(FlagInfo(ShortName('b'), None, None, false, false))))))

  def requiredMissingB: Result =
    run((flag[String](short('a'), Flags.empty) |@| flag[String](short('b'), Flags.empty))(_ -> _), List("-b", "c")).toEither ==== Left(ParseErrorMissing(ParseTreeLeaf(FlagInfo(ShortName('a'), None, None, false, false))))

  def requiredMissingC: Result =
    run((flag[String](short('a'), Flags.empty) |@| flag[String](short('b'), Flags.empty))(_ -> _), List("-a", "c")).toEither ==== Left(ParseErrorMissing(ParseTreeLeaf(FlagInfo(ShortName('b'), None, None, false, false))))

  def leftover: Result =
    run(().pure[Parse], List("-a")) ==== ParseErrorLeftOver("-a" :: Nil).left

  def requiredMissingAlts: Result =
    run(flag[String](short('a'), Flags.empty) ||| flag[String](short('b'), Flags.empty), List()).toEither ==== Left(ParseErrorMissing(ParseTreeAlt(List(ParseTreeLeaf(FlagInfo(ShortName('a'), None, None, false, false)), ParseTreeLeaf(FlagInfo(ShortName('b'), None, None, false, false))))))

  def defaultFound: Result =
    run(flag[String](short('a'), Flags.empty).default("c"), List("-a", "b")) ==== "b".right

  def defaultMissing: Result =
    run(flag[String](short('a'), Flags.empty).default("c"), List()) ==== "c".right

  def optionFound: Result =
    run(flag[String](short('a'), Flags.empty).option, List("-a", "b")) ==== Some("b").right

  def optionMissing: Result =
    run(flag[String](short('a'), Flags.empty).option, List()) ==== None.right

  def assignmentFound: Property = for {
    name <- genNonEmptyLongNameString.log("name")
    value <- Gens.genUnicodeString(0, 50).log("value")
  } yield {
    run(flag[String](long(name.s), Flags.empty), List(s"--${name.s}=$value")) ==== value.right
  }

  def assignmentMissing: Property = for {
    name <- Gens.genName.log("name")
    lname <- genNonEmptyLongNameString.filter(l => name.long != Some(l.s)).log("lname")
    value <- Gens.genUnicodeString(0, 50).log("value")
  } yield {
    run(flag[String](name, Flags.empty), List(s"--${lname.s}=$value")).toEither.isLeft ==== true
  }

  def switchesOn: Result =
    run(switch(short('a'), Flags.empty), List("-a")) ==== true.right

  def switchesOff: Result =
    run(switch(short('a'), Flags.empty), Nil) ==== false.right

  def multipleSwitches: Result =
    run((switch(short('a'), Flags.empty) |@| switch(short('b'), Flags.empty))(_ -> _), List("-ab")) ==== (true, true).right

  def flagAfterSwitch: Result =
    run((switch(short('a'), Flags.empty) |@| flag[String](short('b'), Flags.empty))(_ -> _), List("-ab", "c")) ==== (true, "c").right

  def shortFlagPost: Result =
    run(flag[String](short('a'), Flags.empty), List("-ab")) ==== "b".right

  def flagsLength: Result =
    run(terminator(short('t'), Flags.empty, ()).many.map(_.length), List("-ttt")) ==== 3.right

  def positionalArgs: Result =
    run((argument[String](metavar("src")) |@| argument[String](metavar("dst")))(_ -> _), List("/tmp/src", "tmp/dst")) ==== ("/tmp/src", "tmp/dst").right

  def manyArgs: Property = for {
    args <- Gens.genUnicodeString(1, 20).list(Range.linear(0, 20)).log("args")
  } yield {
    run(arguments[String](metavar("files")), "--" :: args) ==== args.right
  }

  def positionalFollowingMany: Property = for {
    args <- Gens.genUnicodeString(1, 20).list(Range.linear(1, 20)).log("args")
  } yield {
    run((argument[String](metavar("src")) |@| arguments[String](metavar("dst")))(_ -> _), "--" :: args) ==== (args.head, args.tail).right
  }

  // FIXME: This is actually broken. If it is fixed, this test fails.
  def manyFollowingPositional: Property = for {
    args <- Gens.genUnicodeString(1, 20).list(Range.linear(1, 20)).log("args")
  } yield {
    Result.assert(
      run((arguments[String](metavar("dst")) |@| argument[String](metavar("src")))(_ -> _), "--" :: args) != (args.init, args.last).right
    ).log("FIXME: This works now so please fix the test.")
  }

  def someFailsOnEmpty: Result = run(argument[String](metavar("files")).some, List()).toEither.isLeft ==== true

  def invalidOpt: Result = {
    run(flag[String](short('a'), Flags.empty), List("-c")) ==== ParseErrorInvalidOption("-c").left
  }

  def invalidArg: Result = {
    run(flag[String](short('a'), Flags.empty), List("file.txt")) ==== ParseErrorInvalidArgument("file.txt").left
  }

  def intArgString: Result = {
    run(argument[Int](metavar("src")), List("file.txt")) ==== ParseErrorMessage("Error parsing `file.txt` as `Int`").left
  }

  def missingArg: Result = {
    run(argument[Int](metavar("src")), Nil).toEither.isLeft ==== true
  }

  def dobacktrack: Result = Interpreter.run((subcommand(().pure[Parse] ~ "first") |@| switch(short('a'), Flags.empty))(_ -> _),
    "first" :: "-a" :: Nil, DefaultPrefs()) ==== (("first" :: Nil) -> ((), true).right)

  def donotbacktrack: Result = Interpreter.run((subcommand(().pure[Parse] ~ "first") |@| switch(short('a'), Flags.empty))(_ -> _),
    "first" :: "-a" :: Nil, DefaultPrefs().copy(backtrack=false)) ==== (("first" :: Nil) -> ParseErrorLeftOver("-a" :: Nil).left)

  def donotbacktrackbutstillwork: Result = Interpreter.run((subcommand(().pure[Parse] ~ "first") |@| switch(short('a'), Flags.empty))(_ -> _),
    "-a" :: "first" :: Nil, DefaultPrefs().copy(backtrack=false)) ==== (("first" :: Nil) -> ((), true).right)

  def orFirst: Property = for {
    nameOne <- genNonEmptyLongNameString.log("nameOne")
    nameTwo <- genNonEmptyLongNameString.filter(name2 => nameOne.s != name2.s).log("nameTwo")
  } yield {
    run((testA(nameOne.s) ||| testB(nameTwo.s)) , List(s"--${nameOne.s}")) ==== TestA.right
  }


  def orSecond: Property = for {
    nameOne <- genNonEmptyLongNameString.log("nameOne")
    nameTwo <- genNonEmptyLongNameString.filter(name2 => nameOne.s != name2.s).log("nameTwo")
  } yield {
    run((testA(nameOne.s) ||| testB(nameTwo.s)) , List(s"--${nameTwo.s}")) ==== TestB.right
  }

  def wrappers: Property = for {
    name <- genNonEmptyLongNameString.log("name")
  } yield {
    run(wrap(testA(name.s)), List(s"--${name.s}")) ==== TestWrapper(TestA).right
  }

  def subcontext: Result = Interpreter.run(subcommand(subcommand(subcommand(().pure[Parse] ~ "third" ) ~ "second" ) ~ "first"),
    "first" :: "second" :: "third" :: Nil, DefaultPrefs()) ==== (("first" :: "second" :: "third" :: Nil) -> ().right)

  case class LongNameString(s: String)
  def genNonEmptyLongNameString: Gen[LongNameString] = for {
    c <- Gen.alpha
    s <- Gens.genUnicodeString(0, 20).filter(!_.contains("="))
  } yield LongNameString(c.toString + s)
}
