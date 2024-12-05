package metrics.domain

import java.util.UUID
import metrics.ports.MetricsService
import metrics.ports.persistence.*
import metrics.ports.*
import metrics.domain.model.*

class MetricsServiceImpl(
    private val counterEventsRepo: IncrementCounterEventsRepository,
    private val endpointsRepo: MonitoredEndpointsRepository
) extends MetricsService:

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
    ()

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
