import Dependencies._

Build.Settings.project

name := "core"

libraryDependencies ++= Seq(
    akka.actor,
    akka.slf4j,
    akka.http,
    akka.stream,
    akka.httpCore,
    akka.json,
    typesafeConfig,
    argonaut.lib,
    kamon.lib,
    logback,
    scalatest % "test",
    akka.testkit % "test")