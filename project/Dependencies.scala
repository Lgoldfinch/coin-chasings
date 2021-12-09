import sbt._

object Dependencies {

  private object Version {
    val Cats              = "2.7.0"
    val Circe = "0.14.1"
    val CatsEffect        = "3.3.0"
    val Doobie = "1.0.0-RC1"
    val Enumeratum = "1.7.0"
    val Http4s = "0.23.6"
    val PureConfig = "0.17.0"
    val Refined = "0.9.27"
    val NewType           = "0.4.4"
    val Fs2Kafka          = "2.2.0"

    // Test
    val TestContainers = "0.39.12"
    val CatsEffectTest = "1.0.6"
    val Http4sTest = "0.9.1"
    val MunitTest = "0.7.29"

    // Plugins
    val BetterMonadicFor = "0.3.1"
    val KindProjector = "0.13.2"
    val OrganizeImports = "0.6.0"
  }

  val Cats = List(
    "org.typelevel" %% "cats-core" % Version.Cats
  )

  val CatsEffect = List(
    "org.typeLevel" %% "cats-effect" % Version.CatsEffect
  )

  val Circe = List(
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-generic-extras",
    "io.circe" %% "circe-refined"
  ).map(_ % Version.Circe)

  val NewType = List(
    "io.estatico" %% "newtype" % Version.NewType
  )

  val Fs2Kafka = List(
    "com.github.fd4s" %% "fs2-kafka" % Version.Fs2Kafka
  )

  val PureConfig = List(
    "com.github.pureconfig" %% "pureconfig"             % Version.PureConfig,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % Version.PureConfig
  )

  val Refined = List(
    "eu.timepit" %% "refined" % Version.Refined
  )

  // Test
  val CatsEffectTest = List(
    "org.typelevel" %% "munit-cats-effect-3" % Version.CatsEffectTest
  )

  val Http4sTest = List(
    "com.alejandrohdezma" %% "http4s-munit" % Version.Http4sTest
  )

  val MunitTest = List(
    "org.scalameta" %% "munit"              % Version.MunitTest,
    "org.scalameta" %% "munit-scalacheck"   % Version.MunitTest,
    "eu.timepit"    %% "refined-scalacheck" % Version.Refined
  )

  val TestContainers = List(
//    "com.dimafeng" %% "testcontainers-scala-kafka",
    "com.dimafeng" %% "testcontainers-scala-munit",
    "com.dimafeng" %% "testcontainers-scala-postgresql"
  ).map(_ % Version.TestContainers)

  val BetterMonadicFor = "com.olegpy" %% "better-monadic-for" % Version.BetterMonadicFor
  val OrganizeImports = "com.github.liancheng" %% "organize-imports" % Version.OrganizeImports
}
