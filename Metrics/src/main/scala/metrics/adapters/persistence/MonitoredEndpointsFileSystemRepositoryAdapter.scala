package metrics.adapters.persistence

import metrics.domain.model.*
import metrics.ports.persistence.MonitoredEndpointsRepository
import shared.adapters.persistence.FileSystemRepositoryAdapter
import shared.technologies.persistence.FileSystemDatabase
import upickle.default.*
import java.net.URI
import shared.ports.persistence.exceptions.*

class MonitoredEndpointsFileSystemRepositoryAdapter(db: FileSystemDatabase)
    extends MonitoredEndpointsRepository:

  given ReadWriter[URI] =
    readwriter[String].bimap[URI](
      uri => uri.toString(),
      string => URI(string)
    )

  given ReadWriter[Endpoint] = ReadWriter.derived
  given ReadWriter[MonitoredEndpointStatus] = ReadWriter.derived
  given ReadWriter[MonitoredEndpoint] = ReadWriter.derived

  private[MonitoredEndpointsFileSystemRepositoryAdapter] val repo =
    FileSystemRepositoryAdapter[Endpoint, MonitoredEndpoint](
      db,
      "monitored-endpoints"
    )

  export repo.*
