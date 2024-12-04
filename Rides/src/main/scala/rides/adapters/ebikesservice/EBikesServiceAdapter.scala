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

  private case class EBikeIdWrapper(id: EBikeId)
  private given RootJsonFormat[EBikeIdWrapper] = jsonFormat1(
    EBikeIdWrapper.apply
  )

  private val ebikesEndpoint = s"http://$address/ebikes"

  override def find(id: EBikeId): Future[Option[EBikeId]] =
    for
      res <- Http().singleRequest(
        HttpRequest(uri = s"$ebikesEndpoint/${id.value}")
      )
      bike <- res.status match
        case StatusCodes.NotFound => Future(None)
        case _ => Unmarshal(res).to[EBikeIdWrapper].map(Some(_))
    yield bike.map(_.id)

  override def eBikes(): Future[Iterable[EBikeId]] =
    for
      res <- Http().singleRequest(
        HttpRequest(uri = ebikesEndpoint)
      )
      bikes <- Unmarshal(res).to[Array[EBikeIdWrapper]]
    yield bikes.map(_.id)
