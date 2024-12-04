package rides.domain;

import java.util.Date
import scala.concurrent.*
import rides.domain.model.*
import rides.domain.errors.*
import rides.domain.errors.UserOrEBikeAlreadyOnARide.*
import rides.ports.persistence.RidesRepository
import rides.ports.*
import rides.domain.errors.UserOrEBikeAlreadyOnARide.EBikeAlreadyOnARide

class RidesServiceImpl(
    private val ridesRepository: RidesRepository,
    private val eBikesService: EBikesService
)(using
    executionContext: ExecutionContext
) extends RidesService:

  def find(id: RideId): Option[Ride] =
    ridesRepository.find(id)

  def activeRides(): Iterable[Ride] =
    ridesRepository.getAll().filter(_.end.isEmpty)

  def startRide(
      eBikeId: EBikeId,
      username: Username
  ): Future[Either[UserOrEBikeAlreadyOnARide, Ride]] =
    val activeRides = this.activeRides()
    activeRides.exists(_.eBikeId == eBikeId) match
      case true => Future(Left(EBikeAlreadyOnARide(eBikeId)))
      case false =>
        activeRides.exists(_.username == username) match
          case true  => Future(Left(UserAlreadyOnARide(username)))
          case false =>
            // TODO: check for user and ebike existence on other services
            // TODO: random ride id
            val id = RideId("id")
            val ride = Ride(id, eBikeId, username, Date(), None)
            ridesRepository.insert(id, ride) match
              case Left(value)  => ??? // TODO: should not happen
              case Right(value) => Future(Right(ride))

  def endRide(id: RideId): Either[RideNotFound, Ride] =
    ridesRepository.update(
      id,
      r => r.copy(end = r.end.orElse(Some(Date())))
    ) match
      case Left(value)  => Left(RideNotFound(id))
      case Right(value) => Right(value)

  def availableEBikes(): Future[Iterable[EBikeId]] =
    for
      allEBikes <- eBikesService.eBikes()
      eBikesInUse = activeRides().map(_.eBikeId)
    yield (allEBikes.toSet -- eBikesInUse)
