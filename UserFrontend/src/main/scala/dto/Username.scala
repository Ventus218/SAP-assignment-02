package dto

import upickle.default.*

final case class Username(value: String) derives ReadWriter
