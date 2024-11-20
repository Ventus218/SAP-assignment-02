package users.domain.model;

import upickle.default.*

case class User(username: Username, credit: Credit) derives ReadWriter
