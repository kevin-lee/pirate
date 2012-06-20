package io.mth.pirate

import scalaz.{Failure, Success}

object Demo {
  import io.mth.pirate._

  case class DemoArgs(help: Boolean, version: Boolean, verbose: Boolean, things: List[String])

  val helpmode = mode[DemoArgs] <|>
      flag('h', "help", "display usage.")(_.copy(help = true))

  val versionmode = mode[DemoArgs] <|>
      flag('V', "version", "display version.")(_.copy(version = true))

  val basemode = mode[DemoArgs] <|>
      flag('v', "verbose", "verbose output, this has a really long description to demonstrate wrapping.")(_.copy(verbose = true)) >|
      positional1plus("THINGS")((d, ss) => d.copy(things = ss))

  val cmd =
    commandline("demo", "", helpmode :: versionmode :: basemode :: Nil)

  val program =
    cmd ~ """
      | This is the interactive demo. Send it an option and
      | get some output.
    """.stripMargin

  def run(args: DemoArgs): Unit = {
    val output =
      if (args.help)
        program.usage
      else if (args.version)
        "demo version 1"
      else if (args.verbose)
        "this is the verbose output of the demo program [\n" + args + "\n]"
      else
        args.toString
    println(output)
  }

  def main(args: Array[String]) {
    val default = DemoArgs(false, false, false, List())

    val exitcode = cmd.dispatchOrUsage(args.toList, default)(run _)


  }
}
