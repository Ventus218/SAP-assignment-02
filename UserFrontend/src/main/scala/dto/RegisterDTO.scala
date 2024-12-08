package dto

import upickle.default.*

final case class RegisterDTO(username: Username, password: String)
    derives ReadWriter
