name := "content-relationships-graph"

version := "0.1"

scalaVersion := "2.11.12"

resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.anormcypher" %% "anormcypher" % "0.10.0",
  "com.gu" %% "content-api-client-default" % "12.18"
)