import sbt._
import Keys._

object Build {
    object Settings {
        val root: Seq[Setting[_]] = Defaults.defaultConfigs ++ Seq(
            version in ThisBuild := "0.1.5")

        val project = Defaults.defaultConfigs ++
            Defaults.itSettings ++
            BaseSettings.baseSettings ++
            BaseSettings.projectSettings ++
            PublishSettings.publishSettings ++
            Classpaths.ivyPublishSettings ++ Classpaths.jvmPublishSettings
    }

    def generateVersion(major: String, minor: String, snapshot: Boolean) = {
        // Include the git version sha in the build version for repeatable historical builds.
        val gitHeadCommitSha = settingKey[String]("current git commit SHA")
        val incremental = Process("git rev-parse HEAD").lines.head
        s"$major.$minor-$incremental${if (snapshot) "-SNAPSHOT"}"
    }
}