import sbt._
import Keys._
import sbtrelease._

object PublishSettings {
    lazy val publishSettings: Seq[Def.Setting[_]] = Seq(
        // useGpg := true,
        // usePgpKeyHex("4D5CA6F0"),
        publishArtifact in (Compile, packageDoc) := false,
        publishMavenStyle := true,
        pomIncludeRepository := { _ ⇒ false },
        publishTo := {
            val nexus = "https://oss.sonatype.org/"
            if (isSnapshot.value)
                Some("snapshots" at nexus + "content/repositories/snapshots")
            else
                Some("releases" at nexus + "service/local/staging/deploy/maven2")
        },
        pomExtra := (
            <url>https://github.com/ferrlin/</url>
 			 <licenses>
			    <license>
			      <name>BSD-style</name>
			      <url>http://www.opensource.org/licenses/bsd-license.php</url>
			      <distribution>repo</distribution>
			    </license>
			  </licenses>
			  <scm>
			    <url>https://github.com/ferrlin/aktic.git</url>
			    <connection>scm:git:https://github.com/ferrlin/aktic.git</connection>
			  </scm>
			  <developers>
			    <developer>
			      <id>ferrlin</id>
			      <name>John Ferrolino</name>
			      <url>http://blog.ferrl.in</url>
			      </developer>
			  </developers>))

}
