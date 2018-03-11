import sbt._
import Keys._

lazy val commonSettings = Seq(
  organization := "com.kstreee.ci",
  version := "1.0.0",
  scalaVersion := "2.12.4",

  // Scala options
  scalaSource in Compile := baseDirectory.value / "src/main/scala",
  scalaSource in Test := baseDirectory.value / "test/main/scala",

  // Java options
  javacOptions ++= Seq("-source", "1.8"),
  javaSource in Compile := baseDirectory.value / "src/main/java",
  javaSource in Test := baseDirectory.value / "test/main/java",

  // Resource options
  resourceDirectory in Compile := baseDirectory.value / "src/main/resources",
  resourceDirectory in Test := baseDirectory.value / "test/main/resources",

  // Resolvers
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  resolvers += DefaultMavenRepository,

  // Logging libraries
  libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",
  libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  // Scalaz
  libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.19",
  // Play framework
  libraryDependencies += "com.typesafe.play" %% "play" % "2.6.11",
  libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.3",
  // Testing framework
  libraryDependencies += "org.specs2" %% "specs2-core" % "4.0.3" % Test,

  // Clean
  cleanFiles += file("bin"),

  // sbt-assembly settings
  test in assembly := {}
)

lazy val jarOutputPath = settingKey[File]("Common library output path.")
def jarOutputPathSetting(defaultPath: File, binaryName: String) = Seq(
  jarOutputPath := Option(System.getProperty("jarOutputPath"))
    .map { path => file(path) / binaryName }
    .getOrElse { defaultPath / binaryName }
)

lazy val common = (project in file("./app/common")).
  settings(commonSettings: _*).
  settings(jarOutputPathSetting(file("bin"), "common.jar"): _*).
  settings(
    name := "common",
    // sbt-assembly settings
    assemblyOutputPath in assembly := jarOutputPath.value
  )

lazy val cli = (project in file("./app/cli")).
  settings(commonSettings: _*).
  settings(jarOutputPathSetting(file("bin"), "cli.jar"): _*).
  settings(
    name := "cli",
    // Specify main class
    mainClass in Compile := Some("com.kstreee.ci.app.CLIApp"),

    // sbt-assembly settings
    mainClass in assembly := Some("com.kstreee.ci.app.CLIApp"),
    assemblyOutputPath in assembly := jarOutputPath.value
  ).dependsOn(common)

// Explicit build command to provde common library to other build systems such as Gradle.
lazy val buildCommonLibrary = taskKey[Unit]("A task to build a common library")
buildCommonLibrary := (assembly in (common, assembly)).value