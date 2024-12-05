package shared.adapters.persistence

import scala.collection.JavaConverters.*
import shared.ports.persistence.Repository
import shared.ports.persistence.exceptions.*
import shared.technologies.persistence.InMemoryMapDatabase
import scala.util.Try

class InMemoryRepositoryAdapter[ID, T](
    private val db: InMemoryMapDatabase,
    private val entityName: String
) extends Repository[ID, T]:

  private given Conversion[ID, String] with
    def apply(x: ID): String = x.hashCode().toString()

  val map = Try(db.getMap(entityName).asScala)
    .recover({ case e: IllegalStateException =>
      db.createMap(entityName).asScala
    })
    .get
    .asInstanceOf[scala.collection.mutable.Map[String, T]]

  override def getAll(): Iterable[T] =
    this.synchronized:
      map.values

  override def insert(id: ID, entity: T): Either[DuplicateIdException, Unit] =
    this.synchronized:
      map.contains(id) match
        case true =>
          Left(
            DuplicateIdException(s"An $entityName with id $id already exists.")
          )
        case false =>
          map.put(id, entity)
          Right(())

  override def update(id: ID, f: T => T): Either[NotInRepositoryException, T] =
    this.synchronized:
      map.updateWith(id)(_.map(f)) match
        case None =>
          Left(
            NotInRepositoryException(
              s"An $entityName with id $id does not exist."
            )
          )
        case Some(value) => Right(value)

  override def find(id: ID): Option[T] =
    this.synchronized:
      map.get(id)

  override def delete(id: ID): Either[NotInRepositoryException, Unit] =
    this.synchronized:
      map.remove(id) match
        case None =>
          Left(
            NotInRepositoryException(
              s"An $entityName with id $id does not exist."
            )
          )
        case Some(value) => Right(())
