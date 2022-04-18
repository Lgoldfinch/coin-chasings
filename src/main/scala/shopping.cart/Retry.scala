package shopping.cart

import cats.effect.Temporal
import org.typelevel.log4cats.Logger
import shopping.cart.Retry.Retriable

trait Retry[F[_]] {
  def retry[A](
                policy: RetryPolicy[F], retriable: Retriable
              )(fa: F[A]): F[A]
}
object Retry {
  def apply[F[_]: Retry]: Retry[F] = implicitly

  implicit def forLoggerTemporal[F[_]: Logger: Temporal]: Retry[F] =
    new Retry[F] {
      def retry[A](
                    policy: RetryPolicy[F], retriable: Retriable
                  )(fa: F[A]): F[A] = {
        def onError(
                     e: Throwable, details: RetryDetails
                   ): F[Unit] =
          details match {
            case WillDelayAndRetry(_, retriesSoFar, _)  =>
              Logger[F].error(
                s"Failed on ${retriable.show}. We retried $retriesSoFar times."
              )
            case GivingUp(totalRetries, _)  =>
              Logger[F].error(
                s"Giving up on ${retriable.show} after $totalRetries retries."
              ) }
        retryingOnAllErrors[A](policy, onError)(fa)
      }
    }

  sealed trait Retriable
  object Retriable {
    case object Orders   extends Retriable
    case object Payments extends Retriable
  }

  import retry.RetryPolicies._

  val retryPolicy =
    limitRetries[F](3) |+| exponentialBackoff[F](10.milliseconds)
}
