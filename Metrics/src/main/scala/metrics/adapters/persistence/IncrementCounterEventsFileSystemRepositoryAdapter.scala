package metrics.adapters.persistence

import metrics.domain.model.*
import metrics.ports.persistence.IncrementCounterEventsRepository
import shared.adapters.persistence.FileSystemRepositoryAdapter
import shared.technologies.persistence.FileSystemDatabase
import upickle.default.*

class IncrementCounterEventsFileSystemRepositoryAdapter(db: FileSystemDatabase)
    extends IncrementCounterEventsRepository:

  given ReadWriter[CounterId] = ReadWriter.derived
  given ReadWriter[EventId] = ReadWriter.derived
  given ReadWriter[IncrementCounterEvent] = ReadWriter.derived

  private[IncrementCounterEventsFileSystemRepositoryAdapter] val repo =
    FileSystemRepositoryAdapter[EventId, IncrementCounterEvent](
      db,
      "increment-counter-events"
    )

  export repo.*
