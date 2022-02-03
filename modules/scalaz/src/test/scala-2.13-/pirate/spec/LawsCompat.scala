package pirate.spec

trait LawsCompat {
  type LazyCollection[+A] = Stream[A]
}
