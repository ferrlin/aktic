import sbt._
import Keys._

name := "aktic"

// Configuration information used to classify tests based on the time they take to run
lazy val LongRunningTest = config("long") extend Test
lazy val ShortRunningTest = config("short") extend Test

// List of tests that require extra running time (used by CI to stage testing runs)
val longRunningTests = Seq(
    "in.ferrl.aktic.OperationsSpec")

// Aktic
lazy val core = project.in(file("core"))

// Example
lazy val example = project.in(file("sample")).dependsOn(core)

fork in Test := false

fork in IntegrationTest := false

parallelExecution in Test := false

publishLocal := {}

publish := {}