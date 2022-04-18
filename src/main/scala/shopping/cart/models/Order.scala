package shopping.cart.models

import io.estatico.newtype.macros.newtype
import squants.market.Money

import java.util.UUID

@newtype case class OrderId(uuid: UUID)
@newtype case class PaymentId(uuid: UUID)

case class Order(
                  id: OrderId,
                  pid: PaymentId,
                  items: Map[ItemId, Quantity],
                  total: Money
                )
