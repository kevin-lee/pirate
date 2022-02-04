package piratex

import pirate._

object Metavar {

  def rewriteCommand[A](p: Command[A]): Command[A] =
    p.copy(parse = rewriteParse(p.parse))

  def rewriteParse[A](p: Parse[A]): Parse[A] =
    p match {
      case p @ ValueParse(_) =>
        p
      case ParserParse(pr) =>
        ParserParse(pr match {
          case p @ SwitchParser(_, _, _) =>
            p.copy(meta = rewriteMetaFromLongName(p.flag, p.meta))
          case p @ FlagParser(_, _, _) =>
            p.copy(meta = rewriteMetaFromLongName(p.flag, p.meta))
          case p @ ArgumentParser(_, _) =>
            p
          case CommandParser(sub) =>
            CommandParser(rewriteCommand(sub))
        })
      case ApParse(f, a) =>
        ApParse(rewriteParse(f), rewriteParse(a))
      case AltParse(a, b) =>
        AltParse(rewriteParse(a), rewriteParse(b))
      case BindParse(f, a) =>
        BindParse((x: Any) => rewriteParse(f.asInstanceOf[Any => Parse[A]](x)), rewriteParse(a))
    }

  def rewriteMetaFromLongName(name: Name, meta: Metadata): Metadata =
    (meta.metavar, name.long) match {
      case (Some(_), _) =>
        meta
      case (None, None) =>
        meta
      case (None, Some(long)) =>
        meta.copy(metavar = Some(long.replace('-', '_').toUpperCase))
    }
}
