import bintray.Keys._

sbtPlugin := true

organization := "com.typesafe.sbt"

name := "sbt-traceur"

version := "1.0.1"

licenses += ("MIT", url("https://github.com/arielscarpinelli/sbt-traceur/blob/master/LICENSE"))

scalaVersion := "2.10.4"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "org.webjars" % "traceur" % "0.0.79-1"
)

resolvers ++= Seq(
  "Typesafe Releases Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  Resolver.mavenLocal
)

addSbtPlugin("com.typesafe.sbt" % "sbt-js-engine" % "1.0.2")

publishMavenStyle := false

publishTo := {
  if (isSnapshot.value) Some(Classpaths.sbtPluginSnapshots)
  else Some(Classpaths.sbtPluginReleases)
}

bintrayPublishSettings

repository in bintray := "sbt-plugins"

bintrayOrganization in bintray := None

scriptedSettings

scriptedLaunchOpts <+= version apply { v => s"-Dproject.version=$v" }
