package apigateway.ports

import scala.concurrent.Future
import apigateway.domain.model.*
import apigateway.domain.errors.*

trait EBikesService:
  def find(id: EBikeId): Future[Option[EBike]]

  def eBikes(): Future[Iterable[EBike]]

  def register(
      dto: RegisterEBikeDTO
  ): Future[Either[EBikeIdAlreadyInUse, EBike]]
