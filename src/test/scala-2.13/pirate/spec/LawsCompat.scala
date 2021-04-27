package pirate.spec

import scalaz.std.LazyListInstances

trait LawsCompat extends LazyListInstances {
  // TODO: Replace EphemeralStream with LazyList once LazyList support is available in Scalaz.
  type LazyCollection[A] = LazyList[A]
}
