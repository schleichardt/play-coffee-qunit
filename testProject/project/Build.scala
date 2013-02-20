import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "play-coffee-qunit"
  val appVersion = "0.5-SNAPSHOT"

  val appDependencies = Seq(
    "info.schleichardt" %% "play-coffee-qunit" % appVersion
    , "junit" % "junit-dep" % "4.11" % "test" //junit#junit-dep;4.10 is not available in typesafe repo
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    testOptions in Test += Tests.Argument("junitxml", "console")
  ).settings(info.schleichardt.playcoffeequnit.sbt.CoffeeQunitSbtPlugin.buildPipelineSettings(): _*)
}
