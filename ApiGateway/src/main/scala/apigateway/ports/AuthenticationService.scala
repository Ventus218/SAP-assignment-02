package apigateway.ports

import scala.concurrent.Future
import apigateway.domain.model.*

trait AuthenticationService:
  import AuthenticationService.*
  def register(
      dto: RegisterUserDTO
  ): Future[Either[UserAlreadyExists | SomethingWentWrong, String]]

  def authenticate(
      username: Username,
      dto: AuthenticateUserDTO
  ): Future[Either[UserNotFound | UnauthorizedError, String]]

  def refresh(
      bearertoken: String
  ): Future[Either[BadAuthorizationHeader | UnauthorizedError, String]]

  def validate(
      bearertoken: String
  ): Future[Either[BadAuthorizationHeader | UnauthorizedError, Boolean]]

  def forceAuthentication(
      bearertoken: String,
      username: Username
  ): Future[
    Either[BadAuthorizationHeader | UserNotFound | UnauthorizedError, Unit]
  ]

object AuthenticationService:
  case class UserAlreadyExists(username: Username)
  case class UserNotFound(username: Username)
  case class SomethingWentWrong(message: String)
  case class BadAuthorizationHeader()
  case class UnauthorizedError(message: String)
