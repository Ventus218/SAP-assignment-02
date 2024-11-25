ThisBuild / scalaVersion := "3.5.2"

// AKKA HTTP
ThisBuild / resolvers += "Akka library repository".at(
  "https://repo.akka.io/maven"
)
val AkkaVersion = "2.9.3"
val AkkaHttpVersion = "10.6.3"

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

lazy val shared = project
  .in(file("Shared"))
  .settings(
    name := "Shared",
    version := "0.1.0",
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    libraryDependencies += "com.lihaoyi" %% "upickle" % "4.0.2"
  )

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
    libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    assembly / assemblyOutputPath := file("./Users/executable.jar")
  )
  .dependsOn(shared)

lazy val eBikes = project
  .in(file("EBikes"))
  .settings(
    name := "EBikes",
    version := "0.1.0",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    assembly / assemblyOutputPath := file("./EBikes/executable.jar")
  )
  .dependsOn(shared)

import scala.sys.process.*

val allProjectsFilter = ScopeFilter(projects = inAnyProject)

lazy val composeUp =
  taskKey[Any]("Builds the docker images and runs compose up")
composeUp := {
  assembly.all(allProjectsFilter).value
  composeUpProcess() !
}

lazy val composeUpDev = taskKey[Any](
  "Builds the docker images and runs compose up (also loads the docker-compose.dev.yml)"
)
composeUpDev := {
  assembly.all(allProjectsFilter).value
  composeUpProcess("docker-compose.yml", "docker-compose.dev.yml") !
}

def composeUpProcess(composeFiles: String*): ProcessBuilder = {
  val ymlFilesOptions =
    composeFiles.map("-f " + _).reduceOption(_ + " " + _).getOrElse("")
  s"docker compose $ymlFilesOptions build" #&& s"docker compose $ymlFilesOptions up"
}
