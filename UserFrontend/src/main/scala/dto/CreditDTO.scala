package dto

import upickle.default.*

final case class CreditDTO(amount: Int) derives ReadWriter
