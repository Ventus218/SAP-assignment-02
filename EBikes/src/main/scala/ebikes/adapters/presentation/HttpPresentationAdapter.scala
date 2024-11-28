package ebikes.adapters.presentation

import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol.*
import ebikes.domain.model.*
import ebikes.domain.EBikesService
import ebikes.adapters.presentation.dto.*

object HttpPresentationAdapter:

  given RootJsonFormat[V2D] = jsonFormat2(V2D.apply)
  given RootJsonFormat[EBikeId] = jsonFormat1(EBikeId.apply)
  given RootJsonFormat[EBike] = jsonFormat4(EBike.apply)
  given RootJsonFormat[RegisterEBikeDTO] = jsonFormat3(RegisterEBikeDTO.apply)

  def startHttpServer(
      eBikesService: EBikesService,
      host: String,
      port: Int
  ): Future[ServerBinding] =
    implicit val system = ActorSystem(Behaviors.empty, "actor-system")

    val route =
      pathPrefix("ebikes"):
        concat(
          (get & pathEnd):
            complete(eBikesService.eBikes().toArray)
          ,
          (post & pathEnd):
            entity(as[RegisterEBikeDTO]) { dto =>
              eBikesService.register(dto.id, dto.location, dto.direction) match
                case Left(value) =>
                  complete(Conflict, "EBike id already in use")
                case Right(value) => complete(value)
            }
          ,
          path(Segment): eBikeId =>
            get:
              eBikesService.find(EBikeId(eBikeId)) match
                case None        => complete(NotFound, "EBike not found")
                case Some(value) => complete(value)
        )

    Http().newServerAt(host, port).bind(route)
