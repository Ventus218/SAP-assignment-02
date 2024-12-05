package rides.ports;

import scala.concurrent.Future
import rides.domain.model.*;
import rides.domain.errors.*

trait RidesService:

  def find(id: RideId): Option[Ride]

  def activeRides(): Iterable[Ride]

  type StartRideError = UserOrEBikeAlreadyOnARide | UserOrEBikeDoesNotExist |
    FailureInOtherService

  def startRide(
      eBikeId: EBikeId,
      username: Username
  ): Future[Either[StartRideError, Ride]]

  def endRide(id: RideId): Either[RideNotFound, Ride]

  def availableEBikes(): Future[Iterable[EBikeId]]

  // TODO: healthCheck