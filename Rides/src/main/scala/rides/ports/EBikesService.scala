package rides.ports

import scala.concurrent.Future
import rides.domain.model.*
import rides.domain.errors.*

trait EBikesService:
  def find(id: EBikeId): Future[Option[EBikeId]]

  def eBikes(): Future[Iterable[EBikeId]]
