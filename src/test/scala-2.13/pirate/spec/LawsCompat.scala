package pirate.spec

import scalaz.EphemeralStream

trait LawsCompat {
  // TODO: Replace EphemeralStream with LazyList once LazyList support is available in Scalaz.
  type LazyCollection[A] = EphemeralStream[A]
}
