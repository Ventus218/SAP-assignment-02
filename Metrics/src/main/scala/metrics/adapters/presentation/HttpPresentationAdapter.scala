package metrics.adapters.presentation

import java.net.URI
import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol.*
import metrics.domain.model.*
import metrics.ports.MetricsService
import metrics.adapters.presentation.dto.*

object HttpPresentationAdapter:
  import Marshalling.{*, given}

  def startHttpServer(
      metricsService: MetricsService,
      host: String,
      port: Int
  )(using ActorSystem[Any]): Future[ServerBinding] =
    val route =
      pathPrefix("metrics"):
        concat(
          pathPrefix("counters"):
            concat(
              (get & pathEnd):
                complete(
                  metricsService
                    .counters(System.currentTimeMillis())
                    .map((id, value) => CounterDTO(id, value))
                    .toArray
                )
              ,
              path(Segment): segment =>
                val counterId = CounterId(segment)
                concat(
                  get:
                    complete(
                      LongDTO(
                        metricsService
                          .valueOfCounter(counterId, System.currentTimeMillis())
                      )
                    )
                  ,
                  post:
                    entity(as[LongDTO]): amount =>
                      metricsService.incrementCounter(counterId, amount.value)
                      complete(OK, HttpEntity.Empty)
                )
            )
          ,
          pathPrefix("endpoints"):
            concat(
              (get & pathEnd):
                complete(metricsService.monitoredEndpoints().toArray)
              ,
              (post & pathEnd):
                entity(as[Endpoint]): endpoint =>
                  metricsService.startMonitorEndpoint(endpoint)
                  complete(OK, HttpEntity.Empty)
              ,
              path(Segment): segment =>
                val endpoint = Endpoint(URI(segment))
                concat(
                  get:
                    metricsService.monitoredEndpointStatus(endpoint) match
                      case Left(value) =>
                        complete(NotFound, "Endpoint not under monitoring")
                      case Right(value) =>
                        complete(OK, MonitoredEndpointStatusDTO(value))
                  ,
                  delete:
                    metricsService.stopMonitorEndpoint(endpoint) match
                      case Left(_) =>
                        complete(NotFound, "Endpoint not under monitoring")
                      case Right(_) => complete(OK, HttpEntity.Empty)
                )
            )
        )

    Http().newServerAt(host, port).bind(route)

import spray.json.*
import spray.json.JsonWriter.func2Writer
import spray.json.JsonReader.func2Reader
import spray.json.DefaultJsonProtocol.*

private[presentation] object Marshalling:
  given JsonFormat[URI] = jsonFormat[URI](
    func2Reader(js => URI(js.asInstanceOf[JsString].value)),
    func2Writer[URI](uri => JsString(uri.toString()))
  )
  given JsonFormat[MonitoredEndpointStatus] =
    jsonFormat[MonitoredEndpointStatus](
      func2Reader(js =>
        MonitoredEndpointStatus.valueOf(js.asInstanceOf[JsString].value)
      ),
      func2Writer(status => JsString(status.toString()))
    )
  given RootJsonFormat[CounterId] = jsonFormat1(CounterId.apply)
  given RootJsonFormat[EventId] = jsonFormat1(EventId.apply)
  given RootJsonFormat[CounterDTO] = jsonFormat2(CounterDTO.apply)
  given RootJsonFormat[IncrementCounterEvent] = jsonFormat4(
    IncrementCounterEvent.apply
  )
  given RootJsonFormat[Endpoint] = jsonFormat1(Endpoint.apply)
  given RootJsonFormat[LongDTO] = jsonFormat1(LongDTO.apply)
  given RootJsonFormat[MonitoredEndpointStatusDTO] = jsonFormat1(
    MonitoredEndpointStatusDTO.apply
  )
  given RootJsonFormat[MonitoredEndpoint] = jsonFormat2(MonitoredEndpoint.apply)
