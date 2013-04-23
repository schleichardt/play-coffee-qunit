import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "demo"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "info.schleichardt" %% "play-coffee-qunit" % "0.6-SNAPSHOT"
      , "junit" % "junit-dep" % "4.11" % "test" //junit#junit-dep;4.10 is not available in typesafe repo
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
      , coffeescriptOptions := Seq("bare")
    ).settings(info.schleichardt.playcoffeequnit.sbt.CoffeeQunitSbtPlugin.buildPipelineSettings(): _*)

}
