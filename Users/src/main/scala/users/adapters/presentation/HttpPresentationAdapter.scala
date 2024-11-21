package users.adapters.presentation

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol.*
import users.domain.model.*
import users.domain.UsersService
import users.adapters.presentation.dto.*

object HttpPresentationAdapter:

  given RootJsonFormat[Username] = jsonFormat1(Username.apply)
  given RootJsonFormat[Credit] = jsonFormat1(Credit.apply)
  given RootJsonFormat[User] = jsonFormat2(User.apply)
  given RootJsonFormat[RechargeCreditDTO] = jsonFormat2(RechargeCreditDTO.apply)

  def start(usersService: UsersService): Unit =
    implicit val system = ActorSystem(Behaviors.empty, "actor-system")

    val route =
      concat(
        pathPrefix("users"):
          concat(
            post:
              entity(as[Username]) { username =>
                usersService.registerUser(username) match
                  case Left(value)  => complete("Username already in use")
                  case Right(value) => complete(value)
              }
            ,
            path(Segment): username =>
              usersService.checkCredit(Username(username)) match
                case Left(value)  => complete("User not found")
                case Right(value) => complete(value)
          )
        ,
        pathPrefix("auth"):
          get:
            complete("auth")
      )

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
