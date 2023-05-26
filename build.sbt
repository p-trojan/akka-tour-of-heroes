lazy val akkaHttpVersion = "10.4.0"
lazy val akkaVersion    = "2.7.0"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

enablePlugins(JavaAppPackaging, DockerPlugin)

lazy val root = (project in file("."))
  .settings(
    assembly / mainClass := Some("com.example.QuickstartApp"),
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.13.10"
    )),
    name := "akka-http-quickstart-sample",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.2.11",
      "ch.megard"         %% "akka-http-cors"           % "1.1.3",
      "com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % "5.0.0",
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,

      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.2.9"         % Test
    )
  )

Compile / mainClass := Some("com.example.QuickstartApp")