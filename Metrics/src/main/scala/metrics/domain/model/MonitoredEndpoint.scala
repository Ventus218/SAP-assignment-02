package metrics.domain.model

final case class MonitoredEndpoint(
    endpoint: Endpoint,
    status: MonitoredEndpointStatus
)
