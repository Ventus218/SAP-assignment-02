package ebikes

import java.io.File
import scala.sys
import scala.util.Try
import shared.technologies.persistence.FileSystemDatabaseImpl
import shared.adapters.persistence.FileSystemRepositoryAdapter
import ebikes.domain.model.*
import ebikes.domain.EBikesServiceImpl
import ebikes.adapters.presentation.HttpPresentationAdapter
import ebikes.adapters.persistence.EBikesFileSystemRepositoryAdapter

object Main extends App:
  val db = FileSystemDatabaseImpl(File("/data/db"))
  val adapter = EBikesFileSystemRepositoryAdapter(db)
  val eBikesService = EBikesServiceImpl(adapter)
  val host = sys.env.get("HOST").getOrElse("0.0.0.0")
  val port = (for
    portString <- sys.env.get("PORT")
    portInt <- Try(Option(portString.toInt)).getOrElse({
      sys.error("PORT must be an integer"); None
    })
  yield (portInt)).getOrElse(8080)
  HttpPresentationAdapter.startHttpServer(eBikesService, host, port)
  println(s"Listening on $host:$port")
