package shopping.cart

import cats.{Monad, MonadThrow}
import cats.data.NonEmptyList
import shopping.cart.models.{OrderId, UserId}


final case class Checkout[F[_]: Monad](
                                        payments: PaymentsClient[F],
                                        cart: ShoppingCart[F],
                                        orders: Orders[F]
                                      ){


  private def ensureNonEmpty[A](xs: List[A]): F[NonEmptyList[A]] =
    MonadThrow[F].fromOption(
      NonEmptyList.fromList(xs),
      EmptyCartError
    )

  def process(userId: UserId, card: Card): F[OrderId] =
    for {
      c   <- cart.get(userId)
      its <- ensureNonEmpty(c.items)
      pid <- payments.process(Payment(userId, c.total, card))
      oid <- orders.create(userId, pid, its, c.total)
      _   <- cart.delete(userId).attempt.void
    } yield oid
                                      }