package users.domain;

import users.domain.model.*;
import shared.ports.persistence.Repository;
import users.domain.errors.*

class UsersServiceImpl(private val usersRepository: Repository[Username, User])
    extends UsersService:

  override def registerUser(
      username: Username
  ): Either[UsernameAlreadyInUse, User] =
    val user = User(username, Credit(100))
    usersRepository.insert(user.username, user) match
      case Left(value)  => Left(UsernameAlreadyInUse(username))
      case Right(value) => Right(user)

  override def checkCredit(username: Username): Either[UserNotFound, Credit] =
    usersRepository.find(username) match
      case None        => Left(UserNotFound(username))
      case Some(value) => Right(value.credit)

  override def rechargeCredit(
      username: Username,
      rechargeAmount: Credit
  ): Either[UserNotFound, Credit] =
    usersRepository.update(username, _.rechargeCredit(rechargeAmount)) match
      case Left(value)  => Left(UserNotFound(username))
      case Right(value) => Right(value.credit)

  override def users(): Iterable[User] =
    usersRepository.getAll()
