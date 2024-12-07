package apigateway.adapters.presentation

import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import shared.adapters.presentation.HealthCheckError
import shared.ports.MetricsService
import apigateway.domain.model.*
import apigateway.ports.*
import apigateway.adapters.Marshalling.{*, given}
import apigateway.ports.AuthenticationService.*

object HttpPresentationAdapter:

  given RootJsonFormat[HealthCheckError] = jsonFormat1(HealthCheckError.apply)

  private val metricsCounterName = "apigateway_service_requests"

  def startHttpServer(
      apiGatewayService: ApiGatewayService,
      host: String,
      port: Int,
      metricsService: MetricsService
  )(using ActorSystem[Any]): Future[ServerBinding] =
    def incrementMetricsCounter(): Unit =
      metricsService.incrementCounterByOne(metricsCounterName)

    lazy val ebikesRoutes =
      pathPrefix("ebikes"):
        incrementMetricsCounter()
        concat(
          (get & pathEnd):
            onSuccess(apiGatewayService.eBikes_eBikes()): iterable =>
              complete(iterable.toArray)
          ,
          (post & pathEnd):
            entity(as[RegisterEBikeDTO]): dto =>
              onSuccess(apiGatewayService.eBikes_register(dto)):
                _ match
                  case Left(value) =>
                    complete(Conflict, "EBike id already in use")
                  case Right(value) => complete(value)
          ,
          path(Segment): eBikeId =>
            (get & pathEnd):
              onSuccess(apiGatewayService.eBikes_find(EBikeId(eBikeId))):
                _ match
                  case None        => complete(NotFound, "EBike not found")
                  case Some(value) => complete(value)
        )

    lazy val unauthenticatedAuthenticationRoutes =
      pathPrefix("authentication"):
        incrementMetricsCounter()
        concat(
          (path("register") & post):
            entity(as[RegisterUserDTO]): dto =>
              onSuccess(
                apiGatewayService
                  .authentication_register(dto)
              ):
                _ match
                  case Left((UserAlreadyExists(username))) =>
                    complete(Conflict, s"User ${username.value} already exists")
                  case Left((SomethingWentWrong(message))) =>
                    complete(InternalServerError, message)
                  case Right(token) => complete(token)
          ,
          pathPrefix(Segment): segment =>
            val username = Username(segment)
            concat(
              (path("authenticate") & post):
                entity(as[AuthenticateUserDTO]): dto =>
                  onSuccess(
                    apiGatewayService
                      .authentication_authenticate(username, dto)
                  ):
                    _ match
                      case Left(UserNotFound(username)) =>
                        complete(NotFound, s"User $username not found")
                      case Left(UnauthorizedError(message)) =>
                        complete(Unauthorized, message)
                      case Right(token) => complete(token)
            )
        )

    lazy val authenticatedAuthenticationRoutes =
      pathPrefix("authentication"):
        incrementMetricsCounter()
        concat(
          (path(Segment / "forceAuthentication") & post): segment =>
            val username = Username(segment)
            headerValueByName("Authorization"): bearerToken =>
              onSuccess(
                apiGatewayService.authentication_forceAuthentication(
                  bearerToken,
                  username
                )
              ):
                _ match
                  case Left(BadAuthorizationHeader()) =>
                    complete(BadRequest, "Bad Authorization header format")
                  case Left(UserNotFound(username)) =>
                    complete(NotFound, s"User $username not found")
                  case Left(UnauthorizedError(message)) =>
                    complete(Unauthorized, message)
                  case Right(_) => complete(HttpEntity.Empty)
          ,
          (path("refresh") & post):
            headerValueByName("Authorization"): bearerToken =>
              onSuccess(apiGatewayService.authentication_refresh(bearerToken)):
                _ match
                  case Left(BadAuthorizationHeader()) =>
                    complete(BadRequest, "Bad Authorization header format")
                  case Left(UnauthorizedError(message)) =>
                    complete(Unauthorized, message)
                  case Right(token) => complete(token)
        )

    def authenticateRoutes(routes: Route*) =
      pass:
        headerValueByName("Authorization"): bearerToken =>
          onSuccess(apiGatewayService.authentication_validate(bearerToken)):
            _ match
              case Left(BadAuthorizationHeader()) =>
                complete(BadRequest, "Bad Authorization header format")
              case Left(UnauthorizedError(message)) =>
                complete(Unauthorized, message)
              case Right(false) =>
                complete(Unauthorized, "Authentication failed")
              case Right(true) => concat(routes*)

    val route =
      concat(
        pass:
          incrementMetricsCounter()
          concat(
            authenticateRoutes(
              ebikesRoutes,
              authenticatedAuthenticationRoutes
            ),
            unauthenticatedAuthenticationRoutes
          )
        ,
        path("healthCheck"):
          apiGatewayService.healthCheckError() match
            case None => complete(OK, HttpEntity.Empty)
            case Some(value) =>
              complete(ServiceUnavailable, HealthCheckError(value))
      )

    Http().newServerAt(host, port).bind(route)
