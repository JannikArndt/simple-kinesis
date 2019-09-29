name := "fucking-kinesis"
organization := "de.jannikarndt"
version := "0.1.0"
scmInfo := Some(
  ScmInfo(
    url("https://github.com/jannikarndt/fucking-kinesis"),
    "scm:git@github.com:jannikarndt/fucking-kinesis.git"
  )
)
developers := List(
  Developer(
    id = "JannikArndt",
    name = "Jannik Arndt",
    email = "jannik@jannikarndt.de",
    url = url("https://www.jannikarndt.de")
  )
)

publishTo := Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots")
bintrayRepository := "fucking-kinesis"
ThisBuild / turbo := true
ThisBuild / licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
ThisBuild / scalaVersion := "2.13.1"
crossScalaVersions := List("2.13.1", "2.12.10")
Global / onChangedBuildSource := ReloadOnSourceChanges

val awsVersion = "2.9.9"

libraryDependencies ++= Seq(
  "org.scala-lang.modules"     %% "scala-java8-compat"      % "0.9.0",
  "org.scala-lang.modules"     %% "scala-collection-compat" % "2.1.1",
  "io.monix"                   %% "monix-execution"         % "3.0.0",
  "com.typesafe.scala-logging" %% "scala-logging"           % "3.9.2",
  "org.slf4j"                  % "slf4j-simple"             % "1.7.28",
  "software.amazon.awssdk"     % "sts"                      % awsVersion,
  "software.amazon.awssdk"     % "kinesis"                  % awsVersion,
  "software.amazon.awssdk"     % "netty-nio-client"         % awsVersion,
  "org.scalatest"              %% "scalatest"               % "3.0.8" % Test
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding",
  "UTF-8",
  "-Xfatal-warnings",
  "-Ywarn-numeric-widen",
  "-Ywarn-dead-code",
  "-Xlint",
  "-Ywarn-unused:-implicits", // some false positives
  "-Ywarn-value-discard",
  "-Ywarn-macros:after"
)

javacOptions ++= Seq(
  "-source",
  "1.8",
  "-target",
  "1.8"
)
