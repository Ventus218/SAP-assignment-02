package apigateway.ports

import scala.concurrent.Future
import apigateway.domain.model.*
import apigateway.domain.errors.*
import apigateway.ports.AuthenticationService.*

trait ApiGatewayService:
  // EBikes

  def eBikes_find(id: EBikeId): Future[Option[EBike]]

  def eBikes_eBikes(): Future[Iterable[EBike]]

  def eBikes_register(
      dto: RegisterEBikeDTO
  ): Future[Either[EBikeIdAlreadyInUse, EBike]]

  // Authentication

  def authentication_register(
      dto: RegisterUserDTO
  ): Future[Either[UserAlreadyExists | SomethingWentWrong, String]]

  def authentication_authenticate(
      username: Username,
      dto: AuthenticateUserDTO
  ): Future[Either[UserNotFound | UnauthorizedError, String]]

  def authentication_refresh(
      bearertoken: String
  ): Future[Either[BadAuthorizationHeader | UnauthorizedError, String]]

  def authentication_validate(
      bearertoken: String
  ): Future[Either[BadAuthorizationHeader | UnauthorizedError, Boolean]]

  def authentication_forceAuthentication(
      bearertoken: String,
      username: Username
  ): Future[
    Either[BadAuthorizationHeader | UserNotFound | UnauthorizedError, Unit]
  ]

  def healthCheckError(): Option[String]
