package io.mth.pirate.demo

import scalaz.{Failure, Success}
import io.mth.pirate._

object StandardUsageDemo {
  case class DemoArgs(help: Boolean, version: Boolean, verbose: Boolean, things: List[String])

  val basemode = mode[DemoArgs] <|>
      flag('h', "help", "display usage.")(_.copy(help = true)) <|>
      flag('V', "version", "display version.")(_.copy(version = true)) <|>
      flag('v', "verbose", "verbose output, this has a really long description to demonstrate wrapping.")(_.copy(verbose = true)) >|
      positional0plus("THINGS")((d, ss) => d.copy(things = ss))

  val cmd =
    commandline("demo", "", basemode :: Nil)

  def main(ignored: Array[String]) {
    val args = List("--verbose", "thing.one", "thing.two", "cat", "hat")

    val default = DemoArgs(false, false, false, List())

    cmd.dispatchOrDie(args, default) { demo =>
      assert(!demo.version)
      assert(!demo.help)
      assert(demo.verbose)
      assert(demo.things == List("thing.one", "thing.two", "cat", "hat"))
    }
  }
}
