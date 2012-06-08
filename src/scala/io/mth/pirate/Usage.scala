package io.mth.pirate

object Usage {
  /**
   * Build the usage string for the specified command and mode
   * configuration
   */
  def usage[A, _](mode: UsageMode)(pirate: Command[A, _]): String = {
    import Text._

    val flagspace = space(mode.flagIndent)

    def render(p: Command[A, _]): String =
      "Usage:\n" +
        flagspace + p.name + " " + synopsis(p) + "\n" + // FIX smart wrap for synopsis...
        (if (p.description != "") p.description + "\n" else "") +
      "Options: \n" +
        flagspace + "TODO"

    def option(f: Flag[A]) =
      flaguse(f) + "\n" +
        wrap(f.description,  mode.width - mode.descIndent,  mode.descIndent)

    def synopsis(p: Command[A, _]) =
      "TODO"


    def flaguse[A](f: Flag[A]): String =
      f.fold(
        (decls, _, _) => decls.sortWith((a, b) =>
          a.fold(
            c => b.fold(c < _, _ => true),
            s => b.fold(_ => false, s < _)
          )).map(d => d.fold(
            c => "-" + c,
            s => "--" + s
          )).mkString(","),
        (decls, m, _, _) => decls.sortWith((a, b) =>
          a.fold(
            c => b.fold(c < _, _ => true),
            s => b.fold(_ => false, s < _)
          )).map(d => d.fold(
            c => "-" + c,
            s => "--" + s
          )).mkString(",") + "=" + m,
        (decls, m, _, _) => decls.sortWith((a, b) =>
          a.fold(
            c => b.fold(c < _, _ => true),
            s => b.fold(_ => false, s < _)
          )).map(d => d.fold(
            c => "-" + c,
            s => "--" + s
          )).mkString(",") + "=" + m)

    def flagsynopsis[A](f: Flag[A]): String =
      f.fold(
        (decls, _, _) => "[" + decls.sortWith((a, b) =>
          a.fold(
            c => b.fold(c < _, _ => true),
            s => b.fold(_ => false, s < _)
          )).map(d => d.fold(
            c => "-" + c,
            s => "--" + s
          )).mkString("|") + "]",
        (decls, m, _, _) => "[" + decls.sortWith((a, b) =>
          a.fold(
            c => b.fold(c < _, _ => true),
            s => b.fold(_ => false, s < _)
          )).map(d => d.fold(
            c => "-" + c,
            s => "--" + s
          )).mkString("|") + " " + m + "]",
        (decls, m, _, _) => "[" + decls.sortWith((a, b) =>
          a.fold(
            c => b.fold(c < _, _ => true),
            s => b.fold(_ => false, s < _)
          )).map(d => d.fold(
            c => "-" + c,
            s => "--" + s
          )).mkString("|") + " " + m + "]")

    def paramsynopsis[A](p: Positional[A]): String = p.fold(
        (m, f) => m,
        (n, m, f) => (for (_ <- 1 to n) yield m).mkString(" "),
        (m, f) => "[" + m + " ...]",
        (m, f) =>
          if (mode.tightOneOrManySynopsis) m + " ..."
          else m + "[" + m + " ...]"
      )

    render(pirate)
  }
}

/**
 * Usage mode provides configuration options for generating
 * a usage string.
 */
case class UsageMode(
  condenseSynopsis: Boolean,
  flagIndent: Int,
  descIndent: Int,
  width: Int,
  tightOneOrManySynopsis: Boolean
)

/**
 * Default usage mode.
 *  - Explicit synopsis.
 *  - 8/16 indents
 *  - 80 width
 */
object DefaultUsageMode extends UsageMode(
  condenseSynopsis = false,
  flagIndent = 8,
  descIndent = 16,
  width = 80,
  tightOneOrManySynopsis = true
)
