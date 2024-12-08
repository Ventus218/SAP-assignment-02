package metrics.ports

import metrics.domain.model.*

case class CounterNotFound(counterId: CounterId)
case class MonitoredEndpointNotFound(endpoint: Endpoint)

trait MetricsService:
  def incrementCounter(counterId: CounterId, amount: Long): Unit

  def valueOfCounter(counterId: CounterId, atTimestamp: Long): Long

  def counters(atTimestamp: Long): Map[CounterId, Long]

  def startMonitorEndpoint(endpoint: Endpoint): Unit

  def stopMonitorEndpoint(
      endpoint: Endpoint
  ): Either[MonitoredEndpointNotFound, Unit]

  def monitoredEndpointStatus(
      endpoint: Endpoint
  ): Either[MonitoredEndpointNotFound, MonitoredEndpointStatus]
