package rides.ports

import scala.concurrent.Future
import rides.domain.model.*
import rides.domain.errors.*

trait EBikesService:
  import EBikesService.*

  def find(id: EBikeId): Future[Option[EBike]]

  def eBikes(): Future[Iterable[EBikeId]]

  def updatePhisicalData(
      eBikeId: EBikeId,
      dto: UpdateEBikePhisicalDataDTO
  ): Future[Option[EBike]]

object EBikesService:
  case class UpdateEBikePhisicalDataDTO(
      location: V2D,
      direction: V2D,
      speed: Double
  )
