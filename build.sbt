name := "content-relationships-graph"

version := "0.1"

scalaVersion := "2.12.8"

val circeVersion = "0.10.0"

libraryDependencies ++= Seq(
  "com.gu" %% "content-api-client-default" % "12.18",
  "org.jsoup" % "jsoup" % "1.11.3",
  "org.neo4j.driver" % "neo4j-java-driver" % "1.7.3",
  "com.typesafe" % "config" % "1.3.2",
  "io.lemonlabs" %% "scala-uri" % "1.4.4",
  "com.typesafe.akka" %% "akka-http"   % "10.1.8",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",
  "com.typesafe.akka" %% "akka-stream" % "2.5.22",
  "io.spray" %% "spray-json" % "1.3.5",
  "ch.megard" %% "akka-http-cors" % "0.4.0",
  "io.circe"  %% "circe-core"     % circeVersion,
  "io.circe"  %% "circe-generic"  % circeVersion,
  "io.circe"  %% "circe-parser"   % circeVersion
)


//addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.0-RC5")
