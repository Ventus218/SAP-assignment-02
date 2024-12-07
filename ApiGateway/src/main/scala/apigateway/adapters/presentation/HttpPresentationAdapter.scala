package apigateway.adapters.presentation

import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import shared.adapters.presentation.HealthCheckError
import apigateway.domain.model.*
import apigateway.ports.ApiGatewayService
import apigateway.adapters.Marshalling.{*, given}

object HttpPresentationAdapter:

  given RootJsonFormat[HealthCheckError] = jsonFormat1(HealthCheckError.apply)

  def startHttpServer(
      apiGatewayService: ApiGatewayService,
      host: String,
      port: Int
  )(using ActorSystem[Any]): Future[ServerBinding] =
    val route =
      concat(
        pathPrefix("ebikes"):
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
        ,
        path("healthCheck"):
          apiGatewayService.healthCheckError() match
            case None => complete(OK, HttpEntity.Empty)
            case Some(value) =>
              complete(ServiceUnavailable, HealthCheckError(value))
      )

    Http().newServerAt(host, port).bind(route)
