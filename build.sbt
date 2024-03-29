Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / turbo := true

lazy val simplekinesis = (project in file("simple-kinesis"))
  .settings(
    name := "simple-kinesis",
    organization := "de.jannikarndt",
    version := "0.1.0",
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/jannikarndt/simple-kinesis"),
        "scm:git@github.com:jannikarndt/simple-kinesis.git"
      )
    ),
    developers := List(
      Developer(
        id = "JannikArndt",
        name = "Jannik Arndt",
        email = "jannik@jannikarndt.de",
        url = url("https://www.jannikarndt.de")
      )
    ),
    licenses := Seq(("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))),
    scalaVersion := "2.13.1",
    crossScalaVersions := List("2.13.1", "2.12.10"),
    publishTo := Some("Sonatype Snapshots Nexus".at("https://oss.sonatype.org/content/repositories/snapshots")),
    bintrayRepository := "simple-kinesis",
    scalacOptions := scalaCompilerOptions,
    libraryDependencies ++= dependencies
  )

lazy val root = (project in file(".")).aggregate(simplekinesis, examples)

lazy val examples = (project in file("examples"))
  .settings(
    scalaVersion := "2.13.1",
    scalacOptions := scalaCompilerOptions
  )
  .dependsOn(simplekinesis)

val awsVersion = "2.9.24"

lazy val dependencies = Seq(
  "org.scala-lang.modules"     %% "scala-java8-compat"      % "0.9.0",
  "org.scala-lang.modules"     %% "scala-collection-compat" % "2.1.2",
  "io.monix"                   %% "monix-execution"         % "3.0.0",
  "com.typesafe.scala-logging" %% "scala-logging"           % "3.9.2",
  "org.slf4j"                  % "slf4j-simple"             % "1.7.28",
  "software.amazon.awssdk"     % "sts"                      % awsVersion,
  "software.amazon.awssdk"     % "kinesis"                  % awsVersion,
  "software.amazon.awssdk"     % "netty-nio-client"         % awsVersion,
  "org.scalatest"              %% "scalatest"               % "3.0.8" % Test
)

lazy val scalaCompilerOptions = Seq(
  "-target:8",
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8",                         // Specify character encoding used by source files.
  "-explaintypes",                 // Explain type errors in more detail.
  "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds",         // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                   // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",              // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
  "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",       // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",        // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code",              // Warn when dead code is identified.
  "-Ywarn-extra-implicit",         // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",          // Warn when numerics are widened.
  "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",          // Warn if a local definition is unused.
  "-Ywarn-unused:params",          // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",        // Warn if a private member is unused.
  "-Ywarn-value-discard"           // Warn when non-Unit expression results are unused.
)
