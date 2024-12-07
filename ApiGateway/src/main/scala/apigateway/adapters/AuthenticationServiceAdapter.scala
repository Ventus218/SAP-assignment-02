package apigateway.adapters

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import sttp.client4.*
import sttp.model.MediaType
import sttp.model.StatusCode.*
import upickle.default.*
import apigateway.ports.AuthenticationService
import apigateway.ports.AuthenticationService.*
import apigateway.domain.model.*
import Marshalling.{*, given}

class AuthenticationServiceAdapter(address: String)(using ExecutionContext)
    extends AuthenticationService:

  private val baseUrl = s"http://$address/authentication"

  extension [T](r: Request[T])
    def jsonBody(body: String): Request[T] =
      r.body(body).contentType(MediaType.ApplicationJson)
  def register(
      dto: RegisterUserDTO
  ): Future[Either[UserAlreadyExists | SomethingWentWrong, String]] =
    for
      res <- quickRequest
        .post(uri"$baseUrl/register")
        .jsonBody(write(dto))
        .send(DefaultFutureBackend())
      token = res.code match
        case Conflict => Left(UserAlreadyExists(dto.username))
        case InternalServerError =>
          Left(SomethingWentWrong(errorMessage(res.body)))
        case _ => Right(res.body)
    yield (token)

  def authenticate(
      username: Username,
      dto: AuthenticateUserDTO
  ): Future[Either[UserNotFound | UnauthorizedError, String]] =
    for
      res <- quickRequest
        .post(uri"$baseUrl/${username.value}/authenticate")
        .jsonBody(write(dto))
        .send(DefaultFutureBackend())
      token = res.code match
        case NotFound     => Left(UserNotFound(username))
        case Unauthorized => Left(UnauthorizedError(errorMessage(res.body)))
        case _            => Right(res.body)
    yield (token)

  def refresh(
      bearerToken: String
  ): Future[Either[BadAuthorizationHeader | UnauthorizedError, String]] =
    for
      res <- quickRequest
        .post(uri"$baseUrl/refresh")
        .header("Authorization", bearerToken)
        .send(DefaultFutureBackend())
      token = res.code match
        case BadRequest   => Left(BadAuthorizationHeader())
        case Unauthorized => Left(UnauthorizedError(errorMessage(res.body)))
        case _            => Right(res.body)
    yield (token)

  def validate(
      bearerToken: String
  ): Future[Either[BadAuthorizationHeader | UnauthorizedError, Boolean]] =
    for
      res <- quickRequest
        .get(uri"$baseUrl/validate")
        .header("Authorization", bearerToken)
        .send(DefaultFutureBackend())
      result = res.code match
        case BadRequest   => Left(BadAuthorizationHeader())
        case Unauthorized => Left(UnauthorizedError(errorMessage(res.body)))
        case _            => Right(true)
    yield (result)

  def forceAuthentication(
      bearerToken: String,
      username: Username
  ): Future[
    Either[BadAuthorizationHeader | UserNotFound | UnauthorizedError, Unit]
  ] =
    for
      res <- quickRequest
        .post(uri"$baseUrl/${username.value}/forceAuthentication")
        .header("Authorization", bearerToken)
        .send(DefaultFutureBackend())
      result = res.code match
        case BadRequest   => Left(BadAuthorizationHeader())
        case Unauthorized => Left(UnauthorizedError(errorMessage(res.body)))
        case NotFound     => Left(UserNotFound(username))
        case _            => Right(())
    yield (result)

  given ReadWriter[Username] = ReadWriter.derived
  given ReadWriter[RegisterUserDTO] = ReadWriter.derived
  given ReadWriter[AuthenticateUserDTO] = ReadWriter.derived

  private def errorMessage(body: String): String =
    read[Map[String, String]](body)
      .get("message")
      .getOrElse("Something went wrong")
