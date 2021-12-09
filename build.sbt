import sbt._

lazy val coinChaser = (project in file(".")).settings(
  name:= "coin-chasings",
  version:= "0.1.0",
  scalaVersion := "2.13.7",
  libraryDependencies ++= List.concat(
    Dependencies.Cats,
    Dependencies.CatsEffect,
    Dependencies.Circe,
    Dependencies.NewType,
    Dependencies.Refined,
    Dependencies.PureConfig,
    Dependencies.Fs2Kafka
  ) ++ List.concat(
    Dependencies.CatsEffectTest,
    Dependencies.Http4sTest,
    Dependencies.MunitTest,
    Dependencies.TestContainers
  ).map(_ % Test)
)



Global / onChangedBuildSource := ReloadOnSourceChanges
