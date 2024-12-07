ThisBuild / scalaVersion := "3.5.2"
ThisBuild / libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test

// AKKA HTTP
ThisBuild / resolvers += "Akka library repository".at(
  "https://repo.akka.io/maven"
)
val AkkaVersion = "2.9.3"
val AkkaHttpVersion = "10.6.3"
lazy val akkaHttpSettings = Seq(
  libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
)

// Spring Boot
val SpringBootVersion = "3.4.0"
lazy val springBootSettings = Seq(
  javacOptions ++= Seq("-source", "21"),
  javacOptions ++= Seq("-target", "21"),
  libraryDependencies += "org.springframework.boot" % "spring-boot-starter" % SpringBootVersion,
  libraryDependencies += "org.springframework.boot" % "spring-boot-starter-web" % SpringBootVersion,
  libraryDependencies += "org.springframework.boot" % "spring-boot-starter-data-jpa" % SpringBootVersion,
  libraryDependencies += "org.springframework.security" % "spring-security-crypto" % "6.4.1",
  libraryDependencies += "com.mysql" % "mysql-connector-j" % "9.1.0" % Runtime,
  assembly / mainClass := Some("authentication.Application"),
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs @ _*) =>
      xs match {
        case _
            if xs.contains("MANIFEST.MF") ||
              xs.contains("spring.tooling") ||
              xs.contains("INDEX.LIST") ||
              xs.contains("DEPENDENCIES") =>
          MergeStrategy.discard // Discard unnecessary META-INF files
        case _ =>
          MergeStrategy.concat // Merge other META-INF files
      }
    case PathList("META-INF", "services", xs @ _*) =>
      MergeStrategy.concat // Merge service loader files
    case PathList("module-info.class") =>
      MergeStrategy.discard // Discard Java 9 module information
    case "LICENSE" | "license.txt" | "notice.txt" =>
      MergeStrategy.rename // Use the first occurrence of these files
    case _ =>
      MergeStrategy.deduplicate // Use default strategy for other files
  }
)

lazy val userFrontend = project
  .in(file("UserFrontend"))
  .settings(
    name := "User Frontend",
    version := "0.1.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
    assembly / assemblyOutputPath := file("./ApiGateway/executable.jar")
  )

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
    libraryDependencies += "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M19", // for sttp
    libraryDependencies += "com.lihaoyi" %% "upickle" % "4.0.2"
  )

lazy val apiGateway = project
  .in(file("ApiGateway"))
  .settings(
    name := "API Gateway",
    version := "0.1.0",
    akkaHttpSettings,
    assembly / assemblyOutputPath := file("./ApiGateway/executable.jar")
  )
  .dependsOn(shared)

lazy val authentication = project
  .in(file("Authentication"))
  .settings(
    name := "Authentication",
    version := "0.1.0",
    springBootSettings,
    libraryDependencies += "com.auth0" % "java-jwt" % "4.4.0" % Compile,
    assembly / assemblyOutputPath := file("./Authentication/executable.jar")
  )

lazy val metrics = project
  .in(file("Metrics"))
  .settings(
    name := "Metrics",
    version := "0.1.0",
    akkaHttpSettings,
    assembly / assemblyOutputPath := file("./Metrics/executable.jar")
  )
  .dependsOn(shared)

lazy val rides = project
  .in(file("Rides"))
  .settings(
    name := "Rides",
    version := "0.1.0",
    akkaHttpSettings,
    assembly / assemblyOutputPath := file("./Rides/executable.jar")
  )
  .dependsOn(shared)

lazy val users = project
  .in(file("Users"))
  .settings(
    name := "Users",
    version := "0.1.0",
    akkaHttpSettings,
    assembly / assemblyOutputPath := file("./Users/executable.jar")
  )
  .dependsOn(shared)

lazy val eBikes = project
  .in(file("EBikes"))
  .settings(
    name := "EBikes",
    version := "0.1.0",
    akkaHttpSettings,
    libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.2" % Test,
    assembly / assemblyOutputPath := file("./EBikes/executable.jar")
  )
  .dependsOn(shared)

import scala.sys.process.*

val allProjectsFilter = ScopeFilter(projects = inAnyProject)

lazy val composeUp =
  taskKey[Any]("Builds the docker images and runs compose up")
composeUp := {
  assembly.all(allProjectsFilter).value
  composeUpProcess("production.env") !
}

lazy val composeUpDev = taskKey[Any](
  "Builds the docker images and runs compose up (also loads the docker-compose.dev.yml)"
)
composeUpDev := {
  assembly.all(allProjectsFilter).value
  composeUpProcess(
    "development.env",
    "docker-compose.yml",
    "docker-compose.dev.yml"
  ) !
}

def composeUpProcess(envFile: String, composeFiles: String*): ProcessBuilder = {
  val ymlFilesOptions = composeFiles.map("-f " + _).mkString(" ")
  s"docker compose $ymlFilesOptions --env-file $envFile build" #&& s"docker compose $ymlFilesOptions --env-file $envFile up --force-recreate"
}
