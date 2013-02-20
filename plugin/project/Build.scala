import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "play-coffee-qunit"
    val appVersion      = "0.5-SNAPSHOT"

    val appDependencies = Seq(
      "play" %% "play-test" % play.core.PlayVersion.current //make test libs for compile available
      , "junit" % "junit-dep" % "4.11" //junit#junit-dep;4.10 is not available in typesafe repo, it is not enough to have it in test scope
    )

    val githubPath = "schleichardt/play-coffee-qunit"

    val main = play.Project(appName, appVersion, appDependencies).settings(
      organization := "info.schleichardt",
      publishMavenStyle := true,
      publishArtifact in Test := false,
      publishTo <<= version { (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      },
      pomIncludeRepository := { _ => false },
      pomExtra := (
        <url>https://github.com/{githubPath}</url>
          <licenses>
            <license>
              <name>Apache 2</name>
              <url>http://www.apache.org/licenses/LICENSE-2.0</url>
              <distribution>repo</distribution>
            </license>
          </licenses>
          <scm>
            <url>git@github.com:{githubPath}.git</url>
            <connection>scm:git:git@github.com:{githubPath}.git</connection>
          </scm>
          <developers>
            <developer>
              <id>schleichardt</id>
              <name>Michael Schleichardt</name>
              <url>http://michael.schleichardt.info</url>
            </developer>
          </developers>)
    )

}
