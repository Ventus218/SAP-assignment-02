package ebikes.adapters.presentation.dto

import ebikes.domain.model.*

final case class UpdateEBikePhisicalDataDTO(
    location: V2D,
    direction: V2D,
    speed: Double
)
