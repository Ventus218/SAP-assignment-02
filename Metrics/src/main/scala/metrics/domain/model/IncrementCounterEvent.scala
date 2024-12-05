package metrics.domain.model

final case class IncrementCounterEvent(
    id: EventId,
    counterId: CounterId,
    incrementAmount: Long,
    timestamp: Long
)
