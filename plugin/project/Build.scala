import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "play-coffee-qunit"
    val appVersion      = "0.7-SNAPSHOT"

    val appDependencies = Seq(
      "play" %% "play-test" % play.core.PlayVersion.current % "provided"//make test libs for module compilation available, but don't put it into fat JARs
      , "junit" % "junit-dep" % "4.11" % "test" //junit#junit-dep;4.10 is not available in typesafe repo
      , "ro.isdc.wro4j" % "wro4j-extensions" % "1.6.2" exclude("com.github.lltyk","dojo-shrinksafe") exclude("com.github.sommeri","less4j") exclude("com.google.code.gson","gson") exclude("com.google.javascript","closure-compiler") exclude("javax.servlet","servlet-api") exclude("me.n4u.sass","sass-gems") exclude("nz.co.edmi","bourbon-gem-jar") exclude("org.codehaus.gmaven.runtime","gmaven-runtime-1.7") exclude("org.springframework","spring-web") exclude("org.jruby","jruby-complete")//used for compiling CoffeeScript in tests )
    )

  val githubPath = "schleichardt/play-coffee-qunit"

    val main = play.Project(appName, appVersion, appDependencies).settings(
      sources in (Compile, doc) ~= (_ filter (f => false)),
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
