package rides

import java.io.File
import scala.sys
import scala.util.Try
import scala.concurrent.ExecutionContextExecutor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import shared.technologies.persistence.FileSystemDatabaseImpl
import rides.domain.model.*
import rides.domain.RidesServiceImpl
import rides.adapters.presentation.HttpPresentationAdapter
import rides.adapters.ebikesservice.EBikesServiceAdapter
import rides.adapters.usersservice.UsersServiceAdapter
import rides.adapters.persistence.RidesFileSystemRepositoryAdapter

object Main extends App:
  given actorSystem: ActorSystem[Any] =
    ActorSystem(Behaviors.empty, "actor-system")
  given ExecutionContextExecutor = actorSystem.executionContext

  val db = FileSystemDatabaseImpl(File("/data/db"))
  val adapter = RidesFileSystemRepositoryAdapter(db)
  val eBikesServiceAddress =
    sys.env.get("EBIKES_SERVICE_ADDRESS").getOrElse("localhost:8080")
  val eBikesService = EBikesServiceAdapter(eBikesServiceAddress)
  val usersServiceAddress =
    sys.env.get("USERS_SERVICE_ADDRESS").getOrElse("localhost:8080")
  val usersService = UsersServiceAdapter(usersServiceAddress)
  val ridesService = RidesServiceImpl(adapter, eBikesService, usersService)
  val host = sys.env.get("HOST").getOrElse("0.0.0.0")
  val port = (for
    portString <- sys.env.get("PORT")
    portInt <- Try(Option(portString.toInt)).getOrElse({
      sys.error("PORT must be an integer"); None
    })
  yield (portInt)).getOrElse(8080)

  HttpPresentationAdapter
    .startHttpServer(ridesService, host, port)
    .map(_ => println(s"Rides is listening on $host:$port"))