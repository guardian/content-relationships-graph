name := "content-relationships-graph"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.gu" %% "content-api-client-default" % "12.18",
  "org.jsoup" % "jsoup" % "1.11.3",
  "org.neo4j.driver" % "neo4j-java-driver" % "1.7.3",
  "com.typesafe" % "config" % "1.3.2",
  "io.lemonlabs" %% "scala-uri" % "1.4.4"
)

val circeVersion = "0.10.0"

libraryDependencies ++= Seq(
  "io.circe"  %% "circe-core"     % circeVersion,
  "io.circe"  %% "circe-generic"  % circeVersion,
  "io.circe"  %% "circe-parser"   % circeVersion
)

//addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.0-RC5")
