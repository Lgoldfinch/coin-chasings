package com.godfinch.industries.avarice

import cats.effect.{Concurrent, Sync}
import com.godfinch.industries.avarice.models.{AccountData, ShowAccountResponse}
import org.http4s.Method.GET
import org.http4s.{HttpRoutes, Request}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.http4sLiteralsSyntax

trait Coinbase[F[_]] {
  def getAccountData: F[ShowAccountResponse]
}

object Coinbase { //, req: F[Request[F]]
  def impl[F[_]: Concurrent](c: Client[F]): Coinbase[F] = new Coinbase[F] {
    val dsl = new Http4sClientDsl[F] {}
    import dsl._ ; import ShowAccountResponse._
    override def getAccountData: F[ShowAccountResponse] = c.expect[ShowAccountResponse](GET(uri"https://icanhazdadjoke.com/"))
  }
}