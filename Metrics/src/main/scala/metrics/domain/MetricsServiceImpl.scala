package metrics.domain

import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.*
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.Duration
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.settings.ConnectionPoolSettings
import metrics.ports.MetricsService
import metrics.ports.persistence.*
import metrics.ports.*
import metrics.domain.model.*
import metrics.domain.model.MonitoredEndpointStatus.*
import scala.util.Random

class MetricsServiceImpl(
    private val counterEventsRepo: IncrementCounterEventsRepository,
    private val endpointsRepo: MonitoredEndpointsRepository,
    private val initialMonitoringDelay: FiniteDuration =
      FiniteDuration(5, TimeUnit.SECONDS),
    private val monitoringPeriod: FiniteDuration =
      FiniteDuration(10, TimeUnit.SECONDS)
)(using as: ActorSystem[Any])
    extends MetricsService:

  given ExecutionContext = as.executionContext

  endpointsRepo
    .getAll()
    .foreach(e => scheduleHealthcheck(initialMonitoringDelay, e.endpoint))

  def scheduleHealthcheck(after: FiniteDuration, endpoint: Endpoint): Unit =
    as.scheduler.scheduleOnce(
      after,
      () =>
        (for
          res <- Http().singleRequest(
            HttpRequest(uri = Uri(s"http://${endpoint.value}/healthCheck")),
            settings =
              ConnectionPoolSettings(as).withIdleTimeout(Duration.apply(2, "s"))
          )
          status = res.status.isSuccess match
            case true  => Up
            case false => Down
          _ = endpointsRepo.update(endpoint, e => e.copy(status = status)) match
            case Left(_)  => () // The endpoint monitoring was stopped
            case Right(_) => scheduleHealthcheck(monitoringPeriod, endpoint)
        yield ()).recover({ case _: Any =>
          endpointsRepo.update(endpoint, _.copy(status = Down)) match
            case Left(_)  => () // The endpoint monitoring was stopped
            case Right(_) => scheduleHealthcheck(monitoringPeriod, endpoint)
        })
    )

  override def incrementCounter(
      counterId: CounterId,
      amount: Long
  ): Unit =
    val event = IncrementCounterEvent(
      id = EventId(UUID.randomUUID().toString()),
      counterId = counterId,
      incrementAmount = amount,
      timestamp = System.currentTimeMillis()
    )
    counterEventsRepo.insert(event.id, event) match
      case Left(_)  => throw new Exception("UUID collision... WTF")
      case Right(_) => ()

  override def valueOfCounter(
      counterId: CounterId,
      atTimestamp: Long
  ): Long =
    counterEventsRepo
      .getAll()
      .filter(_.counterId == counterId)
      .filter(_.timestamp <= atTimestamp)
      .foldLeft(0L)(_ + _.incrementAmount)

  override def startMonitorEndpoint(
      endpoint: Endpoint
  ): Unit =
    val monitoredEndpoint =
      MonitoredEndpoint(endpoint, MonitoredEndpointStatus.Unknown)
    endpointsRepo.insert(endpoint, monitoredEndpoint)
    scheduleHealthcheck(FiniteDuration(0, TimeUnit.SECONDS), endpoint)

  override def stopMonitorEndpoint(
      endpoint: Endpoint
  ): Either[MonitoredEndpointNotFound, Unit] =
    endpointsRepo.delete(endpoint) match
      case Left(_)  => Left(MonitoredEndpointNotFound(endpoint))
      case Right(_) => Right(())

  override def monitoredEndpointStatus(
      endpoint: Endpoint
  ): Either[MonitoredEndpointNotFound, MonitoredEndpointStatus] =
    endpointsRepo.find(endpoint) match
      case None    => Left(MonitoredEndpointNotFound(endpoint))
      case Some(m) => Right(m.status)
