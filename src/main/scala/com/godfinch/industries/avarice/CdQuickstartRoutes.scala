package com.godfinch.industries.avarice

import cats.effect.{Sync}
import cats.implicits._
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}

object CdQuickstartRoutes {

  def jokeRoutes[F[_] : Sync](J: Jokes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "joke" =>
        for {
          joke <- J.get
          resp <- Ok(joke)
        } yield resp
    }
  }



  case class Tosh(yes: String)

  implicit val toshEncoder: Encoder[Tosh] = deriveEncoder[Tosh]

  implicit def toshEntityEncoder[F[_]]: EntityEncoder[F, Tosh] = jsonEncoderOf

  def helloWorldRoutes[F[_]: Sync](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- H.hello(HelloWorld.Name(name))
          resp <- Ok(greeting)
        } yield resp
    }
  }
}