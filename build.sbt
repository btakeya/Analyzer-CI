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

lazy val common = (project in file("./app/common")).
  settings(commonSettings: _*).
  settings(
    name := "common",

    // sbt-assembly settings
    assemblyOutputPath in assembly := file("bin/common.jar")
  )

lazy val cli = (project in file("./app/cli")).
  settings(commonSettings: _*).
  settings(
    name := "cli",

    // Specify main class
    mainClass in Compile := Some("com.kstreee.ci.app.CLIApp"),

    // sbt-assembly settings
    mainClass in assembly := Some("com.kstreee.ci.app.CLIApp"),
    assemblyOutputPath in assembly := file("bin/cli.jar")
  ).dependsOn(common)

lazy val jenkins = (project in file("./app/jenkins")).
  settings(commonSettings: _*).
  settings(
    name := "jenkins",

    // Compiler order
    compileOrder := CompileOrder.JavaThenScala,

    // Compile options
    scalacOptions := List("-Yresolve-term-conflict:object"),

    // Resolvers
    resolvers += "jenkins-plugin" at "https://repo.jenkins-ci.org/releases/",
    resolvers += "jenkins-plugin-public" at "https://repo.jenkins-ci.org/public/",

    // Jenkins framework
    libraryDependencies += "org.jenkins-ci.main" % "jenkins-core" % "2.109" % "provided",
    libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.0" % "provided",

    // Specify main class
    mainClass in Compile := Some("com.kstreee.ci.app.JenkinsApp")
  ).dependsOn(common)

lazy val buildJenkins = taskKey[Unit]("A task to build jenkins module")
buildJenkins := {
  val stream: TaskStreams = streams.value

  // Build common to use it
  (assembly in (common, assembly)).value

  // Build cmd
  val basePath = baseDirectory.value / "app" / "jenkins"
  val gradle: String = {
    if (sys.props("os.name").contains("Windows")) (basePath / "gradlew.bat").toString
    else (basePath / "gradlew").toString
  }

  val shortName: String = "analyzer-github-CI"
  val options: List[String] = List(
    "jpi",
    s"-PscalaVersion=${scalaVersion.value}",
    s"-PappVersion=${version.value}",
    s"-Porganization=com.kstreee",
    s"-PshortName=$shortName",
    s"-PcommonJarPath=${(baseDirectory.value / "bin"/ "common.jar").toString}",
    s"-PoutputRelativePath=../../../bin"
  )

  // Trigger gradle
  stream.log.info(s"Building a jenkins module using Gradle.\n${(gradle::options).mkString(" ")}")

  import scala.sys.process.Process
  if (Process(gradle::options, new File(basePath.toString)).! == 0) {
    stream.log.success("Succeeded to build a jenkins module.")
  } else {
    throw new IllegalStateException("Failed to build a jenkins module.")
  }
}
