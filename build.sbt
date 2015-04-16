import com.typesafe.sbt.SbtAspectj._
import com.typesafe.sbt.SbtAspectj.AspectjKeys._

name := "challenge-app"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaJpa,
  "org.hibernate" % "hibernate-entitymanager" % "4.2.12.Final",
  "org.mockito" % "mockito-core" % "1.9.5" % "test" withSources() withJavadoc()
)

libraryDependencies += "commons-net" % "commons-net" % "3.2"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.2"

libraryDependencies += "com.restfb" % "restfb" % "1.6.16"

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.2"

libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1102-jdbc4"

libraryDependencies += "org.aspectj" % "aspectjweaver" % "1.8.4"

libraryDependencies += "org.aspectj" % "aspectjrt"     % "1.8.4"

play.Project.playJavaSettings

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

// splitting sources:
// *.scala --> scalac
// *.java --> AspectJ compiler
// all class files come back together at "products in Compile"
// TODO: managedSources are dropped right now
Seq(aspectjSettings: _*)

verbose in Aspectj := true

showWeaveInfo in Aspectj := true

sourceLevel in Aspectj := "-1.7"

// let all unmanaged java sources be compiled by ajc
//sources in Aspectj <<= (sources in Compile).map(_.filter(_.name.endsWith(".java")))

//sources in Compile <<= (sources in Compile).map(_.filterNot(_.name.endsWith(".java")))


//sources in Compile <++= managedSources in Compile

inputs in Aspectj += compiledClasses.value

// add compiled aspectj class files to the rest

products in Compile := (products in Aspectj).value

products in Runtime := (products in Compile).value

//logLevel := Level.Debug