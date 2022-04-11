package shopping.cart.models

import io.estatico.newtype.macros.newtype
import java.util.UUID


@newtype case class ItemId(value: UUID)
@newtype case class ItemName(value: String)
@newtype case class ItemDescription(value: String)

case class Item(
                 uuid: ItemId,
                 name: ItemName,
                 description: ItemDescription,
                 price: Money,
                 brand: Brand,
                 category: Category
               )
case class CreateItem(
                       name: ItemName,
                       description: ItemDescription,
                       price: Money,
                       brandId: BrandId,
                       categoryId: CategoryId
                     )
case class UpdateItem(
                       id: ItemId,
                       price: Money
                     )
