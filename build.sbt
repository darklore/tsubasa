name := "tsubasa"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.typesafe.slick" %% "slick" % "2.0.1",
  "com.typesafe.play" % "play-slick_2.10" % "0.6.0.1"
)

play.Project.playScalaSettings
