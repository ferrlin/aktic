import sbt._
import Keys._
import sbtassembly.PathList

object BaseSettings extends sbtassembly.AssemblyKeys {
    import sbtassembly.MergeStrategy
    import net.virtualvoid.sbt.graph.Plugin._

    val baseSettings: Seq[Def.Setting[_]] =
        graphSettings ++
            Seq(
                organization := "in.ferrl",
                scalaVersion := "2.11.7",
                version := "0.1.5",
                scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7", "-deprecation", "-unchecked", "-Ywarn-dead-code", "-feature"),
                scalacOptions in (Compile, doc) <++= (name in (Compile, doc), version in (Compile, doc)) map DefaultOptions.scaladoc,
                javacOptions in (Compile, compile) ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint:unchecked", "-Xlint:deprecation", "-Xlint:-options"),
                javacOptions in doc := Seq(),
                javaOptions += "-Xmx2G",
                outputStrategy := Some(StdoutOutput),
                // for One-JAR
                exportJars := true,
                // this will be required for monitoring until Akka monitoring SPI comes in
                fork := true,
                fork in test := true,
                sbtPlugin := false,
                resolvers := ResolverSettings.resolvers,
                assemblyMergeStrategy in assembly := {
                    case "application.conf" ⇒ MergeStrategy.concat
                    case "package-info.class" ⇒ MergeStrategy.concat
                    case PathList(ps @ _*) if ps.last endsWith "package-info.class" ⇒ MergeStrategy.discard
                    case PathList(ps @ _*) if ps.last endsWith "pom.properties" ⇒ MergeStrategy.concat
                    case PathList(ps @ _*) if ps.last endsWith "pom.xml" ⇒ MergeStrategy.discard
                    case x if x.startsWith("META-INF/ECLIPSEF.RSA") ⇒ MergeStrategy.last
                    case x if x.startsWith("META-INF/mailcap") ⇒ MergeStrategy.last
                    case x if x.startsWith("META-INF/mimetypes.default") ⇒ MergeStrategy.last
                    case x if x.startsWith("plugin.properties") ⇒ MergeStrategy.last
                    case PathList("javax", "servlet", xs @ _*) ⇒ MergeStrategy.first
                    case PathList("javax", "transaction", xs @ _*) ⇒ MergeStrategy.first
                    case PathList("javax", "mail", xs @ _*) ⇒ MergeStrategy.first
                    case PathList("javax", "activation", xs @ _*) ⇒ MergeStrategy.first
                    case PathList(ps @ _*) if ps.last endsWith ".html" ⇒ MergeStrategy.first
                    case "log4j.properties" ⇒ MergeStrategy.concat
                    case "unwanted.txt" ⇒ MergeStrategy.discard
                    case x ⇒
                        val oldStrategy = (assemblyMergeStrategy in assembly).value
                        oldStrategy(x)
                })

    val projectSettings: Seq[Def.Setting[_]] =
        Seq(publishArtifact := true)
}