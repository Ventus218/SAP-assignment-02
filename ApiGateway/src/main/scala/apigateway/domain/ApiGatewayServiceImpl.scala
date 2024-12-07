package apigateway.domain

import apigateway.ports.ApiGatewayService
import apigateway.domain.model.EBike
import scala.concurrent.Future
import apigateway.ports.*

class ApiGatewayServiceImpl(
    private val eBikesService: EBikesService,
    private val authenticationService: AuthenticationService
) extends ApiGatewayService:

  export eBikesService.{
    find as eBikes_find,
    eBikes as eBikes_eBikes,
    register as eBikes_register
  }

  export authenticationService.{
    register as authentication_register,
    authenticate as authentication_authenticate,
    refresh as authentication_refresh,
    validate as authentication_validate,
    forceAuthentication as authentication_forceAuthentication
  }

  def healthCheckError(): Option[String] = None
