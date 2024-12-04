package rides.adapters.persistence

import java.util.Date;
import rides.domain.model.*
import rides.ports.persistence.RidesRepository
import shared.adapters.persistence.FileSystemRepositoryAdapter
import shared.technologies.persistence.FileSystemDatabase
import upickle.default.*
import shared.ports.persistence.exceptions.NotInRepositoryException
import shared.ports.persistence.exceptions.DuplicateIdException

class RidesFileSystemRepositoryAdapter(db: FileSystemDatabase)
    extends RidesRepository:

  private case class SerializableRide(
      id: RideId,
      eBikeId: EBikeId,
      username: Username,
      start: Long,
      end: Option[Long]
  ):
    def toRide(): Ride =
      Ride(id, eBikeId, username, Date(start), end.map(Date(_)))

  extension (r: Ride)
    private def toSerializableRide(): SerializableRide =
      SerializableRide(
        r.id,
        r.eBikeId,
        r.username,
        r.start.getTime(),
        r.end.map(_.getTime())
      )

  given ReadWriter[Username] = ReadWriter.derived
  given ReadWriter[EBikeId] = ReadWriter.derived
  given ReadWriter[RideId] = ReadWriter.derived
  private given ReadWriter[SerializableRide] = ReadWriter.derived

  private val repo =
    FileSystemRepositoryAdapter[RideId, SerializableRide](db, "rides")

  override def getAll(): Iterable[Ride] =
    repo.getAll().map(_.toRide())

  override def insert(
      id: RideId,
      entity: Ride
  ): Either[DuplicateIdException, Unit] =
    repo.insert(id, entity.toSerializableRide())

  override def update(
      id: RideId,
      f: Ride => Ride
  ): Either[NotInRepositoryException, Ride] =
    repo.update(id, r => f(r.toRide()).toSerializableRide()).map(_.toRide())

  override def find(id: RideId): Option[Ride] =
    repo.find(id).map(_.toRide())

  override def delete(id: RideId): Either[NotInRepositoryException, Unit] =
    repo.delete(id)
