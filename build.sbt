name := "aktic"

organization := "in.ferrl"

version := "0.1.0"

homepage := Some(url("https://github.com/ferrlin/aktic"))

startYear := Some(2013)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/ferrlin/aktic"),
    "scm:git:https://github.com/ferrlin/aktic.git",
    Some("scm:git:git@github.com:ferrlin/aktic.git")))

scalaVersion := "2.11.4"

scalacOptions ++= Seq(
  "-deprecation", "-unchecked", "-encoding", "UTF-8", "-target:jvm-1.7", "-Xlint" // "-optimise"   // this option will slow your build
  )

scalacOptions ++= Seq(
  "-Yclosure-elim",
  "-Yinline",
  "-feature")

// These language flags will be used only for 2.10.x.
// Uncomment those you need, or if you hate SIP-18, all of them.
scalacOptions <++= scalaVersion map { sv ⇒
  if (sv startsWith "2.10") List(
    "-Xverify", "-Ywarn-all", "-feature", "-language:postfixOps")
  else Nil
}

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

val akka = "2.3.8"
val akkaStream = "1.0-M2"
val spray = "1.3.2"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.typesafe.akka" %% "akka-testkit" % akka % "test",
  "com.typesafe.akka" %% "akka-actor" % akka,
  "com.typesafe.akka" %% "akka-slf4j" % akka,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaStream,
  "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStream,
  "com.typesafe.akka" %% "akka-stream-experimental" % akkaStream,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStream,
  "io.argonaut" %% "argonaut" % "6.0.4",
  "org.json4s" %% "json4s-jackson" % "3.2.9",
  "io.argonaut" %% "argonaut" % "6.0.4",
  "com.typesafe" % "config" % "1.2.0")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io")

packageArchetype.java_server
