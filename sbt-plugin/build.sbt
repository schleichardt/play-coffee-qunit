sbtPlugin := true

name := "coffee-qunit-sbt-plugin"

organization := "info.schleichardt"

version := "0.3-SNAPSHOT"

// Comment to get more information during initialization
logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("play" % "sbt-plugin" % "2.0.4")

publishMavenStyle := true

publishTo := Option(Resolver.file("file",  new File(Path.userHome.absolutePath+"/Projekte/schleichardt.github.com/jvmrepo"))(Resolver.mavenStylePatterns))