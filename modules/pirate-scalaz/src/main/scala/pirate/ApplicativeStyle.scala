package pirate

import scalaz._
import Scalaz._

object ApplicativeStyle extends ApplicativeStyle

trait ApplicativeStyle {
  implicit class Function1ApplicativeStyle[A, B](fab: A => B) {
    @inline
    def |*|[Z[_]](a: Z[A])(implicit Z: Applicative[Z]): Z[B] =
      a.map(fab)
  }

  implicit class Function2ApplicativeStyle[A, B, C](fab: (A, B) => C) {
    @inline
    def |*|[Z[_]](x: (Z[A], Z[B]))(implicit Z: Applicative[Z]): Z[C] =
      Z.apply2(x._1, x._2)(fab)
  }

  implicit class Function3ApplicativeStyle[A, B, C, D](fab: (A, B, C) => D) {
    @inline
    def |*|[Z[_]](x: (Z[A], Z[B], Z[C]))(implicit Z: Applicative[Z]): Z[D] =
      Z.apply3(x._1, x._2, x._3)(fab)
  }

  implicit class Function4ApplicativeStyle[A, B, C, D, E](fab: (A, B, C, D) => E) {
    @inline
    def |*|[Z[_]](x: (Z[A], Z[B], Z[C], Z[D]))(implicit Z: Applicative[Z]): Z[E] =
      Z.apply4(x._1, x._2, x._3, x._4)(fab)
  }

  implicit class Function5ApplicativeStyle[A, B, C, D, E, F](fab: (A, B, C, D, E) => F) {
    @inline
    def |*|[Z[_]](x: (Z[A], Z[B], Z[C], Z[D], Z[E]))(implicit Z: Applicative[Z]): Z[F] =
      Z.apply5(x._1, x._2, x._3, x._4, x._5)(fab)
  }

  implicit class Function6ApplicativeStyle[A, B, C, D, E, F, G](fab: (A, B, C, D, E, F) => G) {
    @inline
    def |*|[Z[_]](x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F]))(implicit Z: Applicative[Z]): Z[G] =
      Z.apply6(x._1, x._2, x._3, x._4, x._5, x._6)(fab)
  }

  implicit class Function7ApplicativeStyle[A, B, C, D, E, F, G, H](fab: (A, B, C, D, E, F, G) => H) {
    @inline
    def |*|[Z[_]](x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G]))(implicit Z: Applicative[Z]): Z[H] =
      Z.apply7(x._1, x._2, x._3, x._4, x._5, x._6, x._7)(fab)
  }

  implicit class Function8ApplicativeStyle[A, B, C, D, E, F, G, H, I](fab: (A, B, C, D, E, F, G, H) => I) {
    @inline
    def |*|[Z[_]](x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G], Z[H]))(implicit Z: Applicative[Z]): Z[I] =
      Z.apply8(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8)(fab)
  }

  implicit class Function9ApplicativeStyle[A, B, C, D, E, F, G, H, I, J](fab: (A, B, C, D, E, F, G, H, I) => J) {
    @inline
    def |*|[Z[_]](x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G], Z[H], Z[I]))(implicit Z: Applicative[Z]): Z[J] =
      Z.apply9(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9)(fab)
  }

  implicit class Function10ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K](fab: (A, B, C, D, E, F, G, H, I, J) => K) {
    @inline
    def |*|[Z[_]](x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G], Z[H], Z[I], Z[J]))(implicit Z: Applicative[Z]): Z[K] =
      Z.apply10(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10)(fab)
  }

  implicit class Function11ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L](
    fab: (A, B, C, D, E, F, G, H, I, J, K) => L
  ) {
    @inline
    def |*|[Z[_]](x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G], Z[H], Z[I], Z[J], Z[K]))(
      implicit Z: Applicative[Z]
    ): Z[L] =
      Z.apply11(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11)(fab)
  }

  implicit class Function12ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L, M](
    fab: (A, B, C, D, E, F, G, H, I, J, K, L) => M
  ) {
    @inline
    def |*|[Z[_]](x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G], Z[H], Z[I], Z[J], Z[K], Z[L]))(
      implicit Z: Applicative[Z]
    ): Z[M] =
      Z.apply12(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12)(fab)
  }

  implicit class Function13ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
    fab: (A, B, C, D, E, F, G, H, I, J, K, L, M) => N
  ) {
    @inline
    def |*|[Z[_]](
      x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G], Z[H], Z[I], Z[J], Z[K], Z[L], Z[M])
    )(implicit Z: Applicative[Z]): Z[N] =
      Z.apply3(
        Z.apply6(x._1, x._2, x._3, x._4, x._5, x._6)((_, _, _, _, _, _)),
        Z.apply6(x._7, x._8, x._9, x._10, x._11, x._12)((_, _, _, _, _, _)),
        x._13
      )((x1, x2, x3) => fab(x1._1, x1._2, x1._3, x1._4, x1._5, x1._6, x2._1, x2._2, x2._3, x2._4, x2._5, x2._6, x3))
  }

  implicit class Function14ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
    fab: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) => O
  ) {
    @inline
    def |*|[Z[_]](
      x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G], Z[H], Z[I], Z[J], Z[K], Z[L], Z[M], Z[N])
    )(implicit Z: Applicative[Z]): Z[O] =
      Z.apply2(
        Z.apply7(x._1, x._2, x._3, x._4, x._5, x._6, x._7)((_, _, _, _, _, _, _)),
        Z.apply7(x._8, x._9, x._10, x._11, x._12, x._13, x._14)((_, _, _, _, _, _, _))
      )((x1, x2) =>
        fab(x1._1, x1._2, x1._3, x1._4, x1._5, x1._6, x1._7, x2._1, x2._2, x2._3, x2._4, x2._5, x2._6, x2._7)
      )
  }

  implicit class Function15ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
    fab: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) => P
  ) {
    @inline
    def |*|[Z[_]](
      x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G], Z[H], Z[I], Z[J], Z[K], Z[L], Z[M], Z[N], Z[O])
    )(implicit Z: Applicative[Z]): Z[P] =
      Z.apply3(
        Z.apply7(x._1, x._2, x._3, x._4, x._5, x._6, x._7)((_, _, _, _, _, _, _)),
        Z.apply7(x._8, x._9, x._10, x._11, x._12, x._13, x._14)((_, _, _, _, _, _, _)),
        x._15
      )((x1, x2, x3) =>
        fab(x1._1, x1._2, x1._3, x1._4, x1._5, x1._6, x1._7, x2._1, x2._2, x2._3, x2._4, x2._5, x2._6, x2._7, x3)
      )
  }

  implicit class Function16ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
    fab: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => Q
  ) {
    @inline
    def |*|[Z[_]](
      x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G], Z[H], Z[I], Z[J], Z[K], Z[L], Z[M], Z[N], Z[O], Z[P])
    )(implicit Z: Applicative[Z]): Z[Q] =
      Z.apply2(
        Z.apply8(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8)((_, _, _, _, _, _, _, _)),
        Z.apply8(x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16)((_, _, _, _, _, _, _, _))
      )((x1, x2) =>
        fab(
          x1._1,
          x1._2,
          x1._3,
          x1._4,
          x1._5,
          x1._6,
          x1._7,
          x1._8,
          x2._1,
          x2._2,
          x2._3,
          x2._4,
          x2._5,
          x2._6,
          x2._7,
          x2._8
        )
      )
  }

  implicit class Function17ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
    fab: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) => R
  ) {
    @inline
    def |*|[Z[_]](
      x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G], Z[H], Z[I], Z[J], Z[K], Z[L], Z[M], Z[N], Z[O], Z[P], Z[Q])
    )(implicit Z: Applicative[Z]): Z[R] =
      Z.apply3(
        Z.apply8(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8)((_, _, _, _, _, _, _, _)),
        Z.apply8(x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16)((_, _, _, _, _, _, _, _)),
        x._17
      )((x1, x2, x3) =>
        fab(
          x1._1,
          x1._2,
          x1._3,
          x1._4,
          x1._5,
          x1._6,
          x1._7,
          x1._8,
          x2._1,
          x2._2,
          x2._3,
          x2._4,
          x2._5,
          x2._6,
          x2._7,
          x2._8,
          x3
        )
      )
  }

  implicit class Function18ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
    fab: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => S
  ) {
    @inline
    def |*|[Z[_]](
      x: (Z[A], Z[B], Z[C], Z[D], Z[E], Z[F], Z[G], Z[H], Z[I], Z[J], Z[K], Z[L], Z[M], Z[N], Z[O], Z[P], Z[Q], Z[R])
    )(implicit Z: Applicative[Z]): Z[S] =
      Z.apply2(
        Z.apply9(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9)((_, _, _, _, _, _, _, _, _)),
        Z.apply9(x._10, x._11, x._12, x._13, x._14, x._15, x._16, x._17, x._18)((_, _, _, _, _, _, _, _, _))
      )((x1, x2) =>
        fab(
          x1._1,
          x1._2,
          x1._3,
          x1._4,
          x1._5,
          x1._6,
          x1._7,
          x1._8,
          x1._9,
          x2._1,
          x2._2,
          x2._3,
          x2._4,
          x2._5,
          x2._6,
          x2._7,
          x2._8,
          x2._9
        )
      )
  }

  implicit class Function19ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
    fab: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) => T
  ) {
    @inline
    def |*|[Z[_]](
      x: (
        Z[A],
        Z[B],
        Z[C],
        Z[D],
        Z[E],
        Z[F],
        Z[G],
        Z[H],
        Z[I],
        Z[J],
        Z[K],
        Z[L],
        Z[M],
        Z[N],
        Z[O],
        Z[P],
        Z[Q],
        Z[R],
        Z[S]
      )
    )(implicit Z: Applicative[Z]): Z[T] =
      Z.apply3(
        Z.apply9(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9)((_, _, _, _, _, _, _, _, _)),
        Z.apply9(x._10, x._11, x._12, x._13, x._14, x._15, x._16, x._17, x._18)((_, _, _, _, _, _, _, _, _)),
        x._19
      )((x1, x2, x3) =>
        fab(
          x1._1,
          x1._2,
          x1._3,
          x1._4,
          x1._5,
          x1._6,
          x1._7,
          x1._8,
          x1._9,
          x2._1,
          x2._2,
          x2._3,
          x2._4,
          x2._5,
          x2._6,
          x2._7,
          x2._8,
          x2._9,
          x3
        )
      )
  }

  implicit class Function20ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
    fab: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T) => U
  ) {
    @inline
    def |*|[Z[_]](
      x: (
        Z[A],
        Z[B],
        Z[C],
        Z[D],
        Z[E],
        Z[F],
        Z[G],
        Z[H],
        Z[I],
        Z[J],
        Z[K],
        Z[L],
        Z[M],
        Z[N],
        Z[O],
        Z[P],
        Z[Q],
        Z[R],
        Z[S],
        Z[T]
      )
    )(implicit Z: Applicative[Z]): Z[U] =
      Z.apply2(
        Z.apply10(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10)((_, _, _, _, _, _, _, _, _, _)),
        Z.apply10(x._11, x._12, x._13, x._14, x._15, x._16, x._17, x._18, x._19, x._20)((_, _, _, _, _, _, _, _, _, _))
      )((x1, x2) =>
        fab(
          x1._1,
          x1._2,
          x1._3,
          x1._4,
          x1._5,
          x1._6,
          x1._7,
          x1._8,
          x1._9,
          x1._10,
          x2._1,
          x2._2,
          x2._3,
          x2._4,
          x2._5,
          x2._6,
          x2._7,
          x2._8,
          x2._9,
          x2._10
        )
      )
  }

  implicit class Function21ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
    fab: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U) => V
  ) {
    @inline
    def |*|[Z[_]](
      x: (
        Z[A],
        Z[B],
        Z[C],
        Z[D],
        Z[E],
        Z[F],
        Z[G],
        Z[H],
        Z[I],
        Z[J],
        Z[K],
        Z[L],
        Z[M],
        Z[N],
        Z[O],
        Z[P],
        Z[Q],
        Z[R],
        Z[S],
        Z[T],
        Z[U]
      )
    )(implicit Z: Applicative[Z]): Z[V] =
      Z.apply3(
        Z.apply10(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10)((_, _, _, _, _, _, _, _, _, _)),
        Z.apply10(x._11, x._12, x._13, x._14, x._15, x._16, x._17, x._18, x._19, x._20)((_, _, _, _, _, _, _, _, _, _)),
        x._21
      )((x1, x2, x3) =>
        fab(
          x1._1,
          x1._2,
          x1._3,
          x1._4,
          x1._5,
          x1._6,
          x1._7,
          x1._8,
          x1._9,
          x1._10,
          x2._1,
          x2._2,
          x2._3,
          x2._4,
          x2._5,
          x2._6,
          x2._7,
          x2._8,
          x2._9,
          x2._10,
          x3
        )
      )
  }

  implicit class Function22ApplicativeStyle[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W](
    fab: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V) => W
  ) {
    @inline
    def |*|[Z[_]](
      x: (
        Z[A],
        Z[B],
        Z[C],
        Z[D],
        Z[E],
        Z[F],
        Z[G],
        Z[H],
        Z[I],
        Z[J],
        Z[K],
        Z[L],
        Z[M],
        Z[N],
        Z[O],
        Z[P],
        Z[Q],
        Z[R],
        Z[S],
        Z[T],
        Z[U],
        Z[V]
      )
    )(implicit Z: Applicative[Z]): Z[W] =
      Z.apply2(
        Z.apply11(x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11)(
          (_, _, _, _, _, _, _, _, _, _, _)
        ),
        Z.apply11(x._12, x._13, x._14, x._15, x._16, x._17, x._18, x._19, x._20, x._21, x._22)(
          (_, _, _, _, _, _, _, _, _, _, _)
        )
      )((x1, x2) =>
        fab(
          x1._1,
          x1._2,
          x1._3,
          x1._4,
          x1._5,
          x1._6,
          x1._7,
          x1._8,
          x1._9,
          x1._10,
          x1._11,
          x2._1,
          x2._2,
          x2._3,
          x2._4,
          x2._5,
          x2._6,
          x2._7,
          x2._8,
          x2._9,
          x2._10,
          x2._11
        )
      )
  }

}
