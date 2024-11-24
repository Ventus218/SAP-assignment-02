package users

import java.io.File
import scala.sys
import scala.util.Try
import shared.technologies.persistence.FileSystemDatabaseImpl
import shared.adapters.persistence.FileSystemRepositoryAdapter
import users.domain.model.*
import users.domain.UsersServiceImpl
import users.adapters.presentation.HttpPresentationAdapter

object Main extends App:
  val db = FileSystemDatabaseImpl(File("/Users/Alessandro/Desktop/users/db"))
  val adapter = FileSystemRepositoryAdapter[Username, User](db, "users")
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
