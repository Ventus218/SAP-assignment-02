val scala3Version = "3.5.2"

lazy val userFrontend = project
  .in(file("./UserFrontend"))
  .settings(
    name := "UserFrontend",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
  )

lazy val adminFrontend = project
  .in(file("./AdminFrontend"))
  .settings(
    name := "AdminFrontend",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
  )

lazy val apiGateway = project
  .in(file("./ApiGateway"))
  .settings(
    name := "ApiGateway",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
  )

lazy val authenticationServer = project
  .in(file("./AuthenticationServer"))
  .settings(
    name := "AuthenticationServer",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
  )

lazy val rides = project
  .in(file("./Rides"))
  .settings(
    name := "Rides",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
  )

lazy val users = project
  .in(file("./Users"))
  .settings(
    name := "Users",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
  )

lazy val eBikes = project
  .in(file("./EBikes"))
  .settings(
    name := "EBikes",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
  )
