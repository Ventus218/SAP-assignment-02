package metrics.adapters.presentation.dto

import metrics.domain.model.*

final case class CounterDTO(id: CounterId, value: Long)
