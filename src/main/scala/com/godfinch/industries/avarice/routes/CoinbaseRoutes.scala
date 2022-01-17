package com.godfinch.industries.avarice.routes

import cats.Functor
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits.toFunctorOps
import com.godfinch.industries.avarice.Coinbase
import org.http4s.{Headers, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import cats.effect._
import cats.syntax.all._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._

import java.time.Instant

object CoinbaseRoutes {
  private type HeaderMapping = (String, String)
  val AuthTokenHeader: HeaderMapping = "CB-AUTH-TOKEN" -> "value"
  val AccessSignHeader: HeaderMapping = "CB-ACCESS-SIGN" -> "value"
  def accessTimeStamp(time: Instant): HeaderMapping = "CB-ACCESS-TIMESTAMP" -> time.toEpochMilli.toString

  def routes[F[_]: Sync](c: Coinbase[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F]{
      case GET -> Root / "api.coinbase.com" / "v2" / "accounts" =>
        for {
          bosh <- c.getAccountData
          time <- Clock[F].realTimeInstant
          resp <- Ok(bosh, AuthTokenHeader , AccessSignHeader, accessTimeStamp(time))
          _  = println(resp)
        } yield resp

//        val asdw: F[(String, String)] = AccessTimeStamp[F].sequence
    }
  }
}
