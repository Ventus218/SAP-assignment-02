package dto

import upickle.default.*

final case class AuthenticateDTO(val password: String) derives ReadWriter
