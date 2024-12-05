package apigateway

import scala.util.Try
import scala.concurrent.ExecutionContextExecutor
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import apigateway.domain.ApiGatewayServiceImpl
import apigateway.adapters.*
import apigateway.adapters.presentation.*

object Main extends App:
  given actorSystem: ActorSystem[Any] =
    ActorSystem(Behaviors.empty, "actor-system")
  given ExecutionContextExecutor = actorSystem.executionContext

  val host = sys.env.get("HOST").getOrElse("0.0.0.0")
  val port = (for
    portString <- sys.env.get("PORT")
    portInt <- Try(Option(portString.toInt)).getOrElse({
      sys.error("PORT must be an integer"); None
    })
  yield (portInt)).getOrElse(8080)

  val eBikesServiceAddress =
    sys.env.get("EBIKES_SERVICE_ADDRESS").getOrElse("localhost:8080")
  val eBikesService = EBikesServiceAdapter(eBikesServiceAddress)

  val service = ApiGatewayServiceImpl(eBikesService)

  HttpPresentationAdapter
    .startHttpServer(service, host, port)
    .map(_ => println(s"APIGateway is listening on $host:$port"))
