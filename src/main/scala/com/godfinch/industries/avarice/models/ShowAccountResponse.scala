package com.godfinch.industries.avarice.models

import cats.effect.kernel.Concurrent
import org.http4s.{EntityDecoder, EntityEncoder}

import java.time.Instant
import java.util.UUID
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.circe._


final case class ShowAccountResponse(data: AccountData)

object ShowAccountResponse {
  implicit val encoder: Encoder[ShowAccountResponse] = deriveEncoder[ShowAccountResponse]
  implicit val decoder: Decoder[ShowAccountResponse] = deriveDecoder[ShowAccountResponse]

  implicit def entityEncoder[F[_]]: EntityEncoder[F, ShowAccountResponse] = jsonEncoderOf
  implicit def entityDecoder[F[_]: Concurrent]: EntityDecoder[F, ShowAccountResponse] = jsonOf[F, ShowAccountResponse]
}

final case class AccountData(id: UUID, name: String, primary: Boolean, `type`: String, currency: String,
                             balance: Balance, createdAt: Instant, updatedAt: Instant, resource: String,
                             resourcePath: String
                     )

object AccountData {
  implicit val encoder: Encoder[AccountData] = deriveEncoder[AccountData]
  implicit val decoder: Decoder[AccountData] = deriveDecoder[AccountData]

  implicit def entityEncoder[F[_]]: EntityEncoder[F, AccountData] = jsonEncoderOf
  implicit def entityDecoder[F[_]: Concurrent]: EntityDecoder[F, AccountData] = jsonOf[F, AccountData]
}

final case class Balance(amount: Double, currency: String)

object Balance {
  implicit val encoder: Encoder[Balance] = deriveEncoder[Balance]
  implicit val decoder: Decoder[Balance] = deriveDecoder[Balance]

  implicit def entityEncoder[F[_]]: EntityEncoder[F, Balance] = jsonEncoderOf
  implicit def entityDecoder[F[_]: Concurrent]: EntityDecoder[F, Balance] = jsonOf[F, Balance]
}

sealed trait PrimaryStatus
case object Primary extends PrimaryStatus
case object NonPrimary extends PrimaryStatus

