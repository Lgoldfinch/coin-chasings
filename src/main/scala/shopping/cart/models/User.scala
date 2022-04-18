package shopping.cart.models

import io.estatico.newtype.macros.newtype

import java.util.UUID

@newtype case class UserName(value: String)
@newtype case class Password(value: String)
@newtype case class EncryptedPassword(value: String)

case class UserWithPassword(
                             id: UserId,
                             name: UserName,
                             password: EncryptedPassword
                           )

@newtype case class UserId(value: UUID)

@newtype case class UserId(value: UUID)
@newtype case class JwtToken(value: String)
case class User(id: UserId, name: UserName) Payments