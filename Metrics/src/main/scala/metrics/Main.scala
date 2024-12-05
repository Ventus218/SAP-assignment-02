package metrics

import java.io.File
import scala.sys
import scala.util.Try
import scala.concurrent.ExecutionContextExecutor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import shared.technologies.persistence.FileSystemDatabaseImpl
import metrics.domain.model.*
import metrics.domain.MetricsServiceImpl
import metrics.adapters.presentation.HttpPresentationAdapter
import metrics.adapters.persistence.*

object Main extends App:
  given actorSystem: ActorSystem[Any] =
    ActorSystem(Behaviors.empty, "actor-system")
  given ExecutionContextExecutor = actorSystem.executionContext

  val db = FileSystemDatabaseImpl(File("/data/db"))
  val countersRepo = IncrementCounterEventsFileSystemRepositoryAdapter(db)
  val endpointsRepo = MonitoredEndpointsFileSystemRepositoryAdapter(db)
  val metricsService = MetricsServiceImpl(countersRepo, endpointsRepo)
  val host = sys.env.get("HOST").getOrElse("0.0.0.0")
  val port = (for
    portString <- sys.env.get("PORT")
    portInt <- Try(Option(portString.toInt)).getOrElse({
      sys.error("PORT must be an integer"); None
    })
  yield (portInt)).getOrElse(8080)

  HttpPresentationAdapter
    .startHttpServer(metricsService, host, port)
    .map(_ => println(s"Metrics is listening on $host:$port"))
