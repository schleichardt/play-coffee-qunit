import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play-coffee-qunit"
    val appVersion      = "0.1-SNAPSHOT"
    lazy val publishingFolder = Path.userHome.absolutePath+"/Projekte/schleichardt.github.com/jvmrepo"

    val appDependencies = Seq(
      "play" %% "play-test" % play.core.PlayVersion.current //make test libs for compile available
    )

    val main = PlayProject(appName, appVersion, appDependencies).settings(
      organization := "info.schleichardt",
      publishTo := Some(Resolver.file("file",  new File(publishingFolder))(Resolver.mavenStylePatterns)),
      publishMavenStyle := true
    )

}
