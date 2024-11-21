package users

import java.io.File
import shared.technologies.persistence.FileSystemDatabaseImpl
import shared.adapters.persistence.FileSystemRepositoryAdapter
import users.domain.model.*
import users.domain.UsersServiceImpl
import users.adapters.presentation.HttpPresentationAdapter

object Main extends App:
  val db = FileSystemDatabaseImpl(File("/Users/Alessandro/Desktop/users/db"))
  val adapter = FileSystemRepositoryAdapter[Username, User](db, "user")
  val usersService = UsersServiceImpl(adapter)
  HttpPresentationAdapter.start(usersService)
