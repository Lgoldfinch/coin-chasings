import sbt._

lazy val coinChaser = (project in file(".")).settings(
  name:= "avarice",
  version:= "0.1.0",
  organization := "com.godfinch.industries",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.13.7",
  libraryDependencies ++= List.concat(
    Dependencies.Http4s,
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


//      "org.scalameta"   %% "munit"               % MunitVersion           % Test,
//      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
//      "org.scalameta"   %% "svm-subs"            % "20.2.0"
//    ),
//    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.0" cross CrossVersion.full),
//    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
//    testFrameworks += new TestFramework("munit.Framework")
//  )


Global / onChangedBuildSource := ReloadOnSourceChanges
