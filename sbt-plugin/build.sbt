sbtPlugin := true

name := "coffee-qunit-sbt-plugin"

organization := "info.schleichardt"

version := "0.3-SNAPSHOT"

// Comment to get more information during initialization
logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("play" % "sbt-plugin" % "2.0.4")
