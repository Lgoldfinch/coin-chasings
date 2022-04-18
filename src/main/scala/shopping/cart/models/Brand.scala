package shopping.cart.models

import io.estatico.newtype.macros.newtype

import java.util.UUID

case class Brand(uuid: BrandId, name: BrandName)

@newtype case class BrandId(value: UUID)
@newtype case class BrandName(value: String)