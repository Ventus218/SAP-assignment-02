package ebikes

import java.io.File
import scala.concurrent.Future
import shared.technologies.persistence.FileSystemDatabaseImpl
import ebikes.domain.EBikesServiceImpl
import ebikes.adapters.presentation.HttpPresentationAdapter
import ebikes.adapters.persistence.EBikesFileSystemRepositoryAdapter
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import shared.adapters.MetricsServiceAdapter

object EBikes:
  def run(dbDir: File, host: String, port: Int)(using
      ActorSystem[Any]
  ): Future[ServerBinding] =
    val db = FileSystemDatabaseImpl(dbDir)
    val adapter = EBikesFileSystemRepositoryAdapter(db)
    val eBikesService = EBikesServiceImpl(adapter)

    val metricsServiceAddress =
      sys.env.get("METRICS_SERVICE_ADDRESS").getOrElse("localhost:8080")
    val metricsService = MetricsServiceAdapter(metricsServiceAddress)

    HttpPresentationAdapter
      .startHttpServer(eBikesService, host, port)
      .map(binding =>
        metricsService.registerForHealthcheckMonitoring(
          sys.env.get("EBIKES_SERVICE_ADDRESS").get
        )
        binding
      )(using summon[ActorSystem[Any]].executionContext)
