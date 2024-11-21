package users

import java.io.File
import shared.technologies.persistence.FileSystemDatabaseImpl
import shared.adapters.persistence.FileSystemRepositoryAdapter
import users.domain.model.*
import users.domain.UsersServiceImpl

object Main extends App:
  val db = FileSystemDatabaseImpl(File("/Users/Alessandro/Desktop/users/db"))
  val adapter = FileSystemRepositoryAdapter[Username, User](db, "user")
  val usersService = UsersServiceImpl(adapter)
  val username = Username("tizio")
  adapter.insert(username, User(username, Credit(100))) match
    case Left(exception) =>
      adapter.update(username, _.rechargeCredit(Credit(10)))
    case Right(value) => println("created")
