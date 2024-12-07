package rides.adapters.ebikesservice

import scala.concurrent.Future
import scala.concurrent.ExecutionContextExecutor
import akka.http.scaladsl.Http
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.*
import rides.domain.model.*
import rides.domain.errors.*
import rides.ports.EBikesService
import rides.adapters.Marshalling.{*, given}

class EBikesServiceAdapter(private val address: String)(using
    actorSystem: ActorSystem[Any]
) extends EBikesService:

  given ExecutionContextExecutor = actorSystem.executionContext

  private given RootJsonFormat[EBikeId] = jsonFormat1(EBikeId.apply)
  private given RootJsonFormat[V2D] = jsonFormat2(V2D.apply)
  private given RootJsonFormat[EBike] = jsonFormat4(EBike.apply)

  private val ebikesEndpoint = s"http://$address/ebikes"

  override def find(id: EBikeId): Future[Option[EBike]] =
    for
      res <- Http().singleRequest(
        HttpRequest(uri = s"$ebikesEndpoint/${id.value}")
      )
      bike <- res.status match
        case StatusCodes.NotFound => Future(None)
        case _                    => Unmarshal(res).to[EBike].map(Some(_))
    yield bike

  override def eBikes(): Future[Iterable[EBikeId]] =
    for
      res <- Http().singleRequest(
        HttpRequest(uri = ebikesEndpoint)
      )
      bikes <- Unmarshal(res).to[Array[EBike]]
    yield bikes.map(_.id)
