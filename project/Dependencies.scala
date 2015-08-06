import sbt._
import Keys._

object Dependencies {
    object akka {
        val version = "2.3.12"
        val streamVersion = "1.0"

        // core akka
        val actor = "com.typesafe.akka" %% "akka-actor" % version
        val testkit = "com.typesafe.akka" %% "akka-testkit" % version
        val slf4j = "com.typesafe.akka" %% "akka-slf4j" % version

        // stream and http
        val stream = "com.typesafe.akka" %% "akka-stream-experimental" % streamVersion
        val http = "com.typesafe.akka" %% "akka-http-experimental" % streamVersion
        val httpCore = "com.typesafe.akka" %% "akka-http-core-experimental" % streamVersion

        val json = "com.typesafe.akka" %% "akka-http-spray-json-experimental" % streamVersion
    }

    object argonaut {
        val version = "6.0.4"
        val lib = "io.argonaut" %% "argonaut" % version
    }

    object kamon {
        val version = "0.4.0"
        val lib = "io.kamon" %% "kamon-core" % version
    }

    val typesafeConfig = "com.typesafe" % "config" % "1.3.0"

    // testing
    val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"

    //logging
    val logback = "ch.qos.logback" % "logback-classic" % "1.1.2"
}