package shopping.cart.models

import squants.market.Money

case class Payment(
                    id: UserId,
                    total: Money,
                    card: Card
                  )