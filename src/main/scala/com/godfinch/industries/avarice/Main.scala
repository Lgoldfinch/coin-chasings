package com.godfinch.industries.avarice

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    CdQuickstartServer.stream[IO].compile.drain.as(ExitCode.Success)
}
