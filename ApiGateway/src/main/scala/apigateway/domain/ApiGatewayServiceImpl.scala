package apigateway.domain

import apigateway.ports.ApiGatewayService
import apigateway.domain.model.EBike
import scala.concurrent.Future
import apigateway.ports.EBikesService

class ApiGatewayServiceImpl(private val eBikesService: EBikesService)
    extends ApiGatewayService:

  export eBikesService.{
    find as eBikes_find,
    eBikes as eBikes_eBikes,
    register as eBikes_register
  }

  def healthCheckError(): Option[String] = None
