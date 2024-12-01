package apigateway.adapters

import scala.concurrent.Future
import scala.concurrent.ExecutionContextExecutor
import akka.http.scaladsl.Http
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.*
import apigateway.domain.model.*
import apigateway.domain.errors.*
import apigateway.ports.EBikesService
import apigateway.adapters.Marshalling.{*, given}

class EBikesServiceAdapter(private val address: String)(using
    actorSystem: ActorSystem[Any]
) extends EBikesService:

  given ExecutionContextExecutor = actorSystem.executionContext

  private val ebikesEndpoint = s"http://$address/ebikes"

  override def find(id: EBikeId): Future[Option[EBike]] =
    for
      res <- Http().singleRequest(
        HttpRequest(uri = s"ebikesEndpoint/${id.value}")
      )
      bike <- res.status match
        case StatusCodes.NotFound => Future(None)
        case _                    => Unmarshal(res).to[EBike].map(Some(_))
    yield bike

  override def eBikes(): Future[Iterable[EBike]] =
    for
      res <- Http().singleRequest(
        HttpRequest(uri = ebikesEndpoint)
      )
      bikes <- Unmarshal(res).to[Array[EBike]]
    yield bikes

  override def register(
      dto: RegisterEBikeDTO
  ): Future[Either[EBikeIdAlreadyInUse, EBike]] =
    for
      body <- Marshal(dto).to[MessageEntity]
      res <- Http().singleRequest(
        HttpRequest(
          method = HttpMethods.POST,
          uri = ebikesEndpoint,
          entity = body
        )
      )
      bike <- res.status match
        case StatusCodes.Conflict => Future(Left(EBikeIdAlreadyInUse(dto.id)))
        case _                    => Unmarshal(res).to[EBike].map(Right(_))
    yield (bike)
