name := "challenge-app"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache
)

libraryDependencies += "commons-net" % "commons-net" % "3.2"

libraryDependencies += "com.restfb" % "restfb" % "1.6.14"

play.Project.playJavaSettings
