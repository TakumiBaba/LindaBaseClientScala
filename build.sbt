name := "LindaBaseClient"

organization := "com.takumibaba"

version := "0.1"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.13" % "test",
  "io.backchat.hookup" % "hookup_2.10" % "0.2.3",
  "com.takumibaba" % "eventemitter_2.10" % "0.1.0"
)

initialCommands := "import com.takumibaba.lindabase.client._"
