package rides.adapters.presentation

import java.util.Date;
import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol.*
import rides.domain.model.*
import rides.ports.RidesService
import rides.adapters.presentation.DateMarshalling.*
import rides.adapters.presentation.dto.*
import rides.domain.errors.*
import rides.domain.errors.UserOrEBikeAlreadyOnARide.*
import rides.domain.errors.UserOrEBikeDoesNotExist.*

object HttpPresentationAdapter:

  given RootJsonFormat[Username] = jsonFormat1(Username.apply)
  given RootJsonFormat[EBikeId] = jsonFormat1(EBikeId.apply)
  given RootJsonFormat[RideId] = jsonFormat1(RideId.apply)
  given RootJsonFormat[Ride] = jsonFormat5(Ride.apply)
  given RootJsonFormat[StartRideDTO] = jsonFormat2(StartRideDTO.apply)

  def startHttpServer(
      ridesService: RidesService,
      host: String,
      port: Int
  )(using ActorSystem[Any]): Future[ServerBinding] =
    val route =
      pathPrefix("rides"):
        concat(
          (path("active") & get):
            complete(ridesService.activeRides().toArray)
          ,
          (path("availableEBikes") & get):
            onSuccess(ridesService.availableEBikes()): availableEBikes =>
              complete(availableEBikes.toArray)
          ,
          (post & pathEnd):
            entity(as[StartRideDTO]) { dto =>
              onSuccess(ridesService.startRide(dto.eBikeId, dto.username)):
                _ match
                  case Left(UserAlreadyOnARide(username)) =>
                    complete(Conflict, s"User ${username.value} already riding")
                  case Left(EBikeAlreadyOnARide(eBikeId)) =>
                    complete(Conflict, s"EBike ${eBikeId.value} already riding")
                  case Left(UserDoesNotExist(user)) =>
                    complete(NotFound, s"User ${user.value} does not exists")
                  case Left(EBikeDoesNotExist(id)) =>
                    complete(NotFound, s"EBike ${id.value} does not exists")
                  case Left(FailureInOtherService(msg)) =>
                    complete(InternalServerError, msg)
                  case Right(value) => complete(value)
            }
          ,
          path(Segment): rideId =>
            concat(
              get:
                ridesService.find(RideId(rideId)) match
                  case None        => complete(NotFound, "Ride not found")
                  case Some(value) => complete(value)
              ,
              (put & pathEnd):
                ridesService.endRide(RideId(rideId)) match
                  case Left(value)  => complete(NotFound, "Ride not found")
                  case Right(value) => complete(value)
            )
        )

    Http().newServerAt(host, port).bind(route)
