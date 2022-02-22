package com.godfinch.industries.avarice.routes

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

import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant

object CoinbaseRoutes {
  private type HeaderMapping = (String, String)
  val hi = MessageDigest.getInstance("SHA-256")
  def ting = Instant.now.toEpochMilli.toString ++ "GET" ++ "/api.coinbase.com/v2/accounts"


  println(ting)
  val hello = String.format(
      "%032x", new BigInteger(1, hi.digest(ting.getBytes("UTF-8")
      )))

  println(hello)

  val AuthTokenHeader: HeaderMapping = "CB-AUTH-KEY" ->
  val AccessSignHeader: HeaderMapping = "CB-ACCESS-SIGN" -> hello
  def accessTimeStamp(time: Instant): HeaderMapping = "CB-ACCESS-TIMESTAMP" -> time.toEpochMilli.toString
//f36b45b6dd3266ed2f673d33ae67253758a26df915fb061a05f170f6d982f1af
  def routes[F[_]: Sync](c: Coinbase[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F]{
      case GET -> Root / "getAccounts" =>
        println("hi")
        for {
          bosh <- c.getAccountData
          time <- Clock[F].realTimeInstant
          resp <- Ok(bosh, AuthTokenHeader , AccessSignHeader, accessTimeStamp(time))
        } yield {
          println(resp)
            resp
        }
    }
  }
}
