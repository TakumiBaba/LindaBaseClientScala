name := "LindaBaseClient"

organization := "com.takumibaba"

version := "0.1"

scalaVersion := "2.10.0"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"


libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.13" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.3-SNAPSHOT"
)

initialCommands := "import com.takumibaba.lindabase.client._"
