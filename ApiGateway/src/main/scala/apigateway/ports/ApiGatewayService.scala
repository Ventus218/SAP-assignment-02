package apigateway.ports

import scala.concurrent.Future
import apigateway.domain.model.*
import apigateway.domain.errors.*

trait ApiGatewayService:

  def eBikes_find(id: EBikeId): Future[Option[EBike]]

  def eBikes_eBikes(): Future[Iterable[EBike]]

  def eBikes_register(
      dto: RegisterEBikeDTO
  ): Future[Either[EBikeIdAlreadyInUse, EBike]]

  def healthCheckError(): Option[String]
