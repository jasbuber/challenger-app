name := "challenge-app"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "org.mockito" % "mockito-core" % "1.9.5" % "test" withSources() withJavadoc()
)

libraryDependencies += "commons-net" % "commons-net" % "3.2"

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.2"

play.Project.playJavaSettings
