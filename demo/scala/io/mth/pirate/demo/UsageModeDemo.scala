package io.mth.pirate.demo

import io.mth.pirate._

object UsageModeDemo {
  case class DemoArgs(help: Boolean, version: Boolean, verbose: Boolean, things: List[String])

  val basemode = mode[DemoArgs] <|>
      flag('h', "help", "display usage.")(_.copy(help = true)) <|>
      flag('V', "version", "display version.")(_.copy(version = true)) <|>
      flag('v', "verbose", "verbose output, this has a really long description to demonstrate wrapping.")(_.copy(verbose = true)) >|
      positional0plus("THINGS")((d, ss) => d.copy(things = ss))

  val cmd =
    commandline("demo", "", basemode :: Nil)

  val customMode = DefaultUsageMode.copy(
        condenseSynopsis = true,
        width = 120
    )


  def main(ignored: Array[String]) {
    println("Default usage string --")
    println(cmd.usage)
    println()
    println("Custom usage string --")
    println(cmd.usageForMode(customMode))
  }
}

