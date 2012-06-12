package io.mth.pirate.demo

object HelloWorldDemo {
  import io.mth.pirate._

  val basemode = mode[Option[String]] <|>
    flag1('g', "greeting", "greeting to print", "GREETING")((_, s) => Some(s))

  val cmd =
    commandline("hello", "", basemode :: Nil)

  val helloworld = commandline("hello", "program to print hello world", basemode :: Nil)

   def main(args: Array[String]) {
     helloworld.dispatchOrUsage(args.toList, None, System.err) { greeting =>
       println(greeting.getOrElse("hello world!"))
     }
   }
}
