package metrics.ports.persistence;

import metrics.domain.model.*
import shared.ports.persistence.Repository;
import shared.ports.persistence.exceptions.*;

trait IncrementCounterEventsRepository
    extends Repository[EventId, IncrementCounterEvent]
