package apigateway.domain.errors

import apigateway.domain.model.EBikeId

final case class EBikeIdAlreadyInUse(id: EBikeId)
