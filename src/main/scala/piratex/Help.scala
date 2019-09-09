package piratex

import pirate._, Pirate._

import scalaz._, Scalaz._

object Help {

  def rewriteCommand[A](cmd: Command[A]): Command[A] =
    cmd.copy(parse = rewriteParse(cmd.parse <* helper))

  def rewriteParse[A](p: Parse[A]): Parse[A] =
    p match {
      case p@ValueParse(_) =>
        p
      case ParserParse(pr) =>
        ParserParse(pr match {
          case p@SwitchParser(_, _, _) =>
            p
          case p@FlagParser(_, _, _) =>
            p
          case p@ArgumentParser(_, _) =>
            p
          case CommandParser(sub) =>
            CommandParser(rewriteCommand(sub))
        })
      case ApParse(f, a) =>
        ApParse(rewriteParse(f), rewriteParse(a))
      case AltParse(a, b) =>
        AltParse(rewriteParse(a), rewriteParse(b))
      case BindParse(f, a) =>
        BindParse((x: Any) => rewriteParse(f(x)), rewriteParse(a))
    }
}
