import sbt._

object ResolverSettings {
    lazy val resolvers = Seq(
        Resolver.defaultLocal,
        Resolver.mavenLocal,
        Resolver.sonatypeRepo("releases"),
        Resolver.typesafeRepo("releases"),
        Resolver.typesafeRepo("snapshots"),
        Resolver.sonatypeRepo("snapshots"))
}