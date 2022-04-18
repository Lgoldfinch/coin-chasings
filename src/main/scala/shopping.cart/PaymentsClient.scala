package shopping.cart

import shopping.cart.models.{Payment, PaymentId}

trait PaymentsClient[F[_]] {
  def process(payment: Payment): F[PaymentId]
}
