// To package as jar
enablePlugins(JavaAppPackaging)

lazy val root = (project in file(".")).
  settings(
    name := "analysis-github-CI",
    version := "1.0.0",

    scalaVersion := "2.12.4",

    scalaSource in Compile := baseDirectory.value / "src/main/scala",
    scalaSource in Test := baseDirectory.value / "test/main/scala",

    resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
    resolvers += "jenkins-plugin" at "https://repo.jenkins-ci.org/releases/",
    resolvers += DefaultMavenRepository,

    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",

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

    // Jenkins plugin
    // libraryDependencies += "org.jenkins-ci.tools" % "maven-hpi-plugin" % "2.2",
    libraryDependencies += "org.jenkins-ci.plugins" % "plugin" % "3.5",

    mainClass in (Compile, run) := Some("com.kstreee.ci.app.cli.CLIApp")
  )
