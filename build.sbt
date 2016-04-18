name := """google-diplomat"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
    jdbc,
    cache,
    ws,
    evolutions,
    specs2 % Test,
    "com.typesafe.play" %% "anorm" % "2.4.0",
    "mysql" % "mysql-connector-java" % "5.1.25",
    "org.postgresql" % "postgresql" % "9.4.1208.jre7",
    "org.scalaz" %% "scalaz-core" % "7.1.0",
    "org.webjars" %% "webjars-play" % "2.3.0-2",
    "org.webjars.bower" % "bootstrap" % "3.3.6",
    "org.webjars.bower" % "jquery" % "2.2.0",
    "org.webjars.bower" % "jquery-ui" % "1.11.4",
    "org.webjars" % "underscorejs" % "1.8.3"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator