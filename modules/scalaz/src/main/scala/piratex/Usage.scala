package piratex

import pirate._

object Usage {

  def render[A](parent: List[String], cmd: Command[A]): List[String] = {
    List(
      List(pirate.Usage.print(cmd.copy(name = (cmd.name :: parent).reverse.mkString(" ")), Nil, DefaultPrefs()))
    , renderParse(cmd.name :: parent, cmd.parse)
    ).flatten
  }

  def renderParse[A](parent: List[String], p: Parse[A]): List[String] =
    p match {
      case ValueParse(_) =>
        Nil
      case ParserParse(pr) =>
        pr match {
          case SwitchParser(_, _, _) =>
            Nil
          case FlagParser(_, _, _) =>
            Nil
          case ArgumentParser(_, _) =>
            Nil
          case CommandParser(sub) =>
            render(parent, sub)
        }
      case ApParse(f, a) =>
        renderParse(parent, f) ++ renderParse(parent, a)
      case AltParse(a, b) =>
        renderParse(parent, a) ++ renderParse(parent, b)
      case BindParse(_, a) =>
        renderParse(parent, a)
    }
}
