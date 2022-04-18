package shopping.cart.models

import io.estatico.newtype.macros.newtype

import java.util.UUID

final case class Category(uuid: CategoryId, name: CategoryName)

@newtype case class CategoryId(value: UUID)
@newtype case class CategoryName(value: String)
