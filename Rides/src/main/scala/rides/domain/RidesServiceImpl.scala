package rides.domain;

import java.util.*
import scala.concurrent.*
import rides.domain.model.*
import rides.domain.errors.*
import rides.domain.errors.UserOrEBikeAlreadyOnARide.*
import rides.domain.errors.UserOrEBikeDoesNotExist.*
import rides.ports.persistence.RidesRepository
import rides.ports.*
import rides.domain.errors.UserOrEBikeAlreadyOnARide.EBikeAlreadyOnARide

class RidesServiceImpl(
    private val ridesRepository: RidesRepository,
    private val eBikesService: EBikesService,
    private val usersService: UsersService
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
  ): Future[Either[StartRideError, Ride]] =
    val activeRides = this.activeRides()

    lazy val bikeIsFree = !activeRides.exists(_.eBikeId == eBikeId)
    lazy val userIsFree = !activeRides.exists(_.username == username)
    val checkEBikeAndUserFree = for
      _ <- Either.cond(bikeIsFree, (), EBikeAlreadyOnARide(eBikeId))
      _ <- Either.cond(userIsFree, (), UserAlreadyOnARide(username))
    yield ()

    (checkEBikeAndUserFree match
      case Left(error) => Future(Left(error))
      case Right(_) =>
        for
          eBikeOpt <- eBikesService.find(eBikeId)
          userExist <- usersService.exist(username)
          eBikeAndUserExist =
            for
              _ <- eBikeOpt.toRight(EBikeDoesNotExist(eBikeId))
              _ <- Either.cond(userExist, (), UserDoesNotExist(username))
            yield ()

          ride = eBikeAndUserExist match
            case Left(error) => Left(error)
            case Right(_) =>
              val id = RideId(UUID.randomUUID().toString())
              val ride = Ride(id, eBikeId, username, Date(), None)
              ridesRepository.insert(id, ride) match
                case Left(value)  => throw Exception("UUID collision... WTF")
                case Right(value) => Right(ride)
        yield (ride)
    ).recover({ case e: Exception =>
      Left(FailureInOtherService())
    })

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

  def healthCheckError(): Option[String] = None
