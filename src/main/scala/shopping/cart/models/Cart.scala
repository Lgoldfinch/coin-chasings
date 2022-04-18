package shopping.cart.models

import io.estatico.newtype.macros.newtype
import squants.market.Money


@newtype case class Quantity(value: Int)
@newtype case class Cart(items: Map[ItemId, Quantity])
case class CartItem(item: Item, quantity: Quantity)
case class CartTotal(items: List[CartItem], total: Money)