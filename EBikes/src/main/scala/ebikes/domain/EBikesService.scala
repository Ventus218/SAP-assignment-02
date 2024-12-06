package ebikes.domain;

import ebikes.domain.model.*;
import ebikes.domain.errors.*

trait EBikesService:

  def find(id: EBikeId): Option[EBike]

  def eBikes(): Iterable[EBike]

  def register(
      id: EBikeId,
      location: V2D,
      direction: V2D
  ): Either[EBikeIdAlreadyInUse, EBike]

  def healthCheckError(): Option[String]
