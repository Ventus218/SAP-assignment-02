ThisBuild / scalaVersion := "3.5.2"

// lazy val userFrontend = project
//   .in(file("UserFrontend"))
//   .settings(
//     name := "User Frontend",
//     version := "0.1.0",
//     libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
//   )

// lazy val adminFrontend = project
//   .in(file("AdminFrontend"))
//   .settings(
//     name := "Admin Frontend",
//     version := "0.1.0",
//     libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test
//   )

lazy val apiGateway = project
  .in(file("ApiGateway"))
  .settings(
    name := "API Gateway",
    version := "0.1.0",
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    assembly / assemblyOutputPath := file("./ApiGateway/executable.jar")
  )

lazy val authentication = project
  .in(file("Authentication"))
  .settings(
    name := "Authentication",
    version := "0.1.0",
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    assembly / assemblyOutputPath := file("./Authentication/executable.jar")
  )

lazy val metrics = project
  .in(file("Metrics"))
  .settings(
    name := "Metrics",
    version := "0.1.0",
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    assembly / assemblyOutputPath := file("./Metrics/executable.jar")
  )

lazy val rides = project
  .in(file("Rides"))
  .settings(
    name := "Rides",
    version := "0.1.0",
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    assembly / assemblyOutputPath := file("./Rides/executable.jar")
  )

lazy val users = project
  .in(file("Users"))
  .settings(
    name := "Users",
    version := "0.1.0",
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    assembly / assemblyOutputPath := file("./Users/executable.jar")
  )

lazy val eBikes = project
  .in(file("EBikes"))
  .settings(
    name := "EBikes",
    version := "0.1.0",
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    assembly / assemblyOutputPath := file("./EBikes/executable.jar")
  )
