package shopping.cart

import shopping.cart.Db.AppStatus

trait HealthCheck[F[_]] {
  def status: F[AppStatus]
}
object Db {
  @derive(encoder)
  @newtype
  case class RedisStatus(value: Status)

  @derive(encoder)
  @newtype
  case class PostgresStatus(value: Status)

  @derive(encoder)
  case class AppStatus(
                        redis: RedisStatus,
                        postgres: PostgresStatus
                      )
}