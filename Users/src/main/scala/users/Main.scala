package users

import java.io.File
import scala.sys
import scala.util.Try
import shared.technologies.persistence.FileSystemDatabaseImpl
import users.domain.model.*
import users.domain.UsersServiceImpl
import users.adapters.presentation.HttpPresentationAdapter
import users.adapters.persistence.UsersFileSystemRepositoryAdapter

object Main extends App:
  // TODO: externalize configuration of DB file path
  val db = FileSystemDatabaseImpl(File("/Users/Alessandro/Desktop/users/db"))
  val adapter = UsersFileSystemRepositoryAdapter(db)
  val usersService = UsersServiceImpl(adapter)
  val host = sys.env.get("HOST").getOrElse("0.0.0.0")
  val port = (for
    portString <- sys.env.get("PORT")
    portInt <- Try(Option(portString.toInt)).getOrElse({
      sys.error("PORT must be an integer"); None
    })
  yield (portInt)).getOrElse(8080)
  HttpPresentationAdapter.startHttpServer(usersService, host, port)
  println(s"Listening on $host:$port")
