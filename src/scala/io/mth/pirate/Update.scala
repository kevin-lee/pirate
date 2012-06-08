package io.mth.pirate

object Update {
  type Update[A, B] = (A, B) => Either[String, A]
}
