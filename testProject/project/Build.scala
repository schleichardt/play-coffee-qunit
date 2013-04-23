import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "play-coffee-qunit"
  val appVersion = "0.7-SNAPSHOT"

  val appDependencies = {
    def seleniumDependency(artifactId: String) = "org.seleniumhq.selenium" % artifactId % "2.30.0" % "test"
    Seq(
      "info.schleichardt" %% "play-coffee-qunit" % appVersion
      , "junit" % "junit-dep" % "4.11" % "test" //junit#junit-dep;4.10 is not available in typesafe repo
      , seleniumDependency("selenium-firefox-driver")
      , seleniumDependency("selenium-htmlunit-driver")
      , "org.apache.httpcomponents" % "httpclient" % "4.2.3" //needed by selenium, must be in compile scope
    )
  }

  val main = play.Project(appName, appVersion, appDependencies).settings(
    testOptions in Test += Tests.Argument("junitxml", "console")
    , logBuffered in Test := false
    , scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature")
  ).settings(info.schleichardt.playcoffeequnit.sbt.CoffeeQunitSbtPlugin.buildPipelineSettings(): _*)
}
