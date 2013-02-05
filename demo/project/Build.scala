import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "demo"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "info.schleichardt" %% "play-coffee-qunit" % "0.4-SNAPSHOT"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
      , coffeescriptOptions := Seq("bare")
    ).settings(info.schleichardt.playcoffeequnit.sbt.CoffeeQunitSbtPlugin.buildPipelineSettings(): _*)

}
