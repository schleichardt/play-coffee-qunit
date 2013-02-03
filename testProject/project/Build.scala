import sbt._
import Keys._
import sbt.PlayProject._

object ApplicationBuild extends Build {

  val appName = "play-coffee-qunit"
  val appVersion = "0.3-SNAPSHOT"

  val appDependencies = Seq(
    "info.schleichardt" %% "play-coffee-qunit" % appVersion
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
  ).settings(info.schleichardt.playcoffeequnit.sbt.CoffeeQunitSbtPlugin.buildPipelineSettings(): _*)
}
