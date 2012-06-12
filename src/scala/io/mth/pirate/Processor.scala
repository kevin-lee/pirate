package io.mth.pirate

object Processor {
  trait Result[A]
  case class Fail[A](msg: String) extends Result[A]
  case class Succeed[A](a: A) extends Result[A]

  def isFlag(arg: String) =
    arg != "-" && arg != "--" && arg.startsWith("-")

  def matches[A](arg: String, f: Flag[A]) =
    f.declarations.exists(_.form == arg)

  def flags[A](fs: Flags[A], init: A, args: List[String]): Result[(A, List[String])] = args match {
    case "--" :: xs =>
      Succeed((init, xs))
    case x :: y :: xs => if (!isFlag(x)) Succeed((init, args)) else fs.toList.find(z => matches(x, z)) match {
      case None =>
        Fail("could not match: " + x)
      case Some(f) => f.fold(
        (_, _, _, u) => u(init, ()) match {
          case Left(msg) => Fail(msg)
          case Right(v) => flags(fs, v, y :: xs)
        },
        (_, _, _, _, u) => u(init, y) match {
          case Left(msg) => Fail(msg)
          case Right(v) => flags(fs, v, xs)
        },

        (_, _, _, _, u) =>
          if (isFlag(y))
            u(init, Some(y)) match {
              case Left(msg) => Fail(msg)
              case Right(v) => flags(fs, v, xs)
            }
          else
            u(init, None) match {
              case Left(msg) => Fail(msg)
              case Right(v) => flags(fs, v, y :: xs)
            }
      )
    }
    case x :: Nil => if (!isFlag(x)) Succeed((init, args)) else fs.toList.find(z => matches(x, z)) match {
      case None => Fail("could not match: " + x)
      case Some(f) => f.fold(
        (_, _, _, u) => u(init, ()) match {
          case Left(msg) => Fail(msg)
          case Right(v) => Succeed((v, Nil))
        },
        (d, m, _, _, _) => Fail("expected argument not supplied for flag: " + (d.map(_.form).mkString("|") + "=" + m)),
        (_, _, _, _, u) => u(init, None) match {
          case Left(msg) => Fail(msg)
          case Right(v) => Succeed((v, Nil))
        }
      )
    }
    case Nil => Succeed((init, args))
  }

  def positionals[A](ps: List[Positional[A]], init: A, args: List[String]): Result[A] = ps match {
    case Nil => if (args.isEmpty) Succeed(init) else Fail("unused arguments: " + args.map('"' + _ + '"').mkString(" "))
    case x :: Nil =>
      x.fold(
        (m, u) => args match {
          case Nil => Fail("required positional argument not specified: " + m)
          case x :: Nil => Succeed(u(init, x))
          case x :: xs => Fail("unexpected positional arguments: " + xs.map('"' + _ + '"').mkString(" "))
        },
        (n, _, u) => {
          val ax = args.take(n)
          if (ax.length != n)
            Fail("insufficient positional arguments specified, required at least: " + n)
          else
            Succeed(u(init, ax))
        },
        (_, u) =>
          Succeed(u(init, args)),
        (_, u) =>
          if (args.isEmpty)
            Fail("insufficient positional arguments specified, required at least: 1")
          else
            Succeed(u(init, args))
      )
    case x :: y :: xs =>
      x.fold(
        (m, u) => args match {
          case Nil => Fail("required positional argument not specified: " + m)
          case z :: zs => positionals(y :: xs, u(init, z), zs)
        },
        (n, _, u) => {
          val ax = args.take(n)
          if (ax.length != n)
            Fail("insufficient positional arguments specified, required at least: " + n)
          else
            positionals(y :: xs, u(init, ax), args.drop(n))
        },
        (_, u) =>
          if ((y :: xs).exists(!_.isFixed))
            Fail("fatal!!! ambiguous command line definition, multiple variable length arguments")
          else {
            val zs = y :: xs
            val weight = zs.foldLeft(0)(_ + _.weight)
            val diff = args.length - weight
            if (diff < 0)
              Fail("insufficient positional arguments specified, required at least: " + weight)
            else
              positionals(zs, u(init, args.take(diff)), args.drop(diff))
          },
        (_, u) =>
          if ((y :: xs).exists(!_.isFixed))
            Fail("fatal!!! ambiguous command line definition, multiple variable length arguments")
          else {
            val zs = y :: xs
            val weight = zs.foldLeft(0)(_ + _.weight)
            val diff = args.length - weight - 1
            if (diff < 0)
              Fail("insufficient positional arguments specified, required at least: " + (weight + 1))
            else
              positionals(zs, u(init, args.take(diff)), args.drop(diff))
          }
      )
  }

  def primary[A, B](fs: Flags[A], ps: Positionals[A], f: A => B, init: A, args: List[String]): Result[B] =
    flags(fs, init, args) match {
      case Fail(msg) => Fail(msg)
      case Succeed((a, xs)) =>
        positionals(ps.toList, a, xs) match {
          case Fail(msg) => Fail(msg)
          case Succeed(aa) => Succeed(f(aa))
        }
    }

  def sub[A, B](fs: Flags[A], cmd: Command[A, B], init: A, args: List[String]): Result[B] =
    flags(fs, init, args) match {
      case Fail(msg) => Fail(msg)
      case Succeed((a, Nil)) => Fail("No sub-mode specified")
      case Succeed((a, x :: xs)) =>
        if (x == cmd.name)
          process(cmd, a, xs)
        else
          Fail("Invalid sub-mode specified")
    }


  def mode[A, B](m: Mode[A, B], init: A, args: List[String]): Result[B] =
    m.fold(primary(_, _, _, init, args), sub(_, _, init, args))

  def process[A, B](cmd: Command[A, B], init: A, args: List[String]): Result[B] =
    cmd.modes.foldLeft[Result[B]](Fail(""))((acc, m) => acc match {
      case Fail(msg) => mode(m, init, args)
      case Succeed(a) => acc
    })

}
