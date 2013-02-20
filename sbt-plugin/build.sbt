sbtPlugin := true

name := "coffee-qunit-sbt-plugin"

organization := "info.schleichardt"

version := "0.5-SNAPSHOT"

// Comment to get more information during initialization
logLevel := Level.Warn

libraryDependencies += "org.specs2" %% "specs2" % "1.9" % "test"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("play" % "sbt-plugin" % "2.1.0")

publishMavenStyle := true

publishArtifact in Test := false

publishTo <<= version { (v: String) =>
val nexus = "https://oss.sonatype.org/"
if (v.trim.endsWith("SNAPSHOT"))
  Some("snapshots" at nexus + "content/repositories/snapshots")
else
  Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

pomExtra := (
    <url>https://github.com/schleichardt/play-coffee-qunit</url>
    <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      <distribution>repo</distribution>
    </license>
    </licenses>
    <scm>
    <url>git@github.com:schleichardt/play-coffee-qunit.git</url>
    <connection>scm:git:git@github.com:schleichardt/play-coffee-qunit.git</connection>
    </scm>
    <developers>
    <developer>
      <id>schleichardt</id>
      <name>Michael Schleichardt</name>
      <url>http://michael.schleichardt.info</url>
    </developer>
    </developers>
)

