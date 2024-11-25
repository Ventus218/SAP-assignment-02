package users.adapters.presentation

import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol.*
import users.domain.model.*
import users.domain.UsersService

object HttpPresentationAdapter:

  given RootJsonFormat[Username] = jsonFormat1(Username.apply)
  given RootJsonFormat[Credit] = jsonFormat1(Credit.apply)
  given RootJsonFormat[User] = jsonFormat2(User.apply)

  def startHttpServer(
      usersService: UsersService,
      host: String,
      port: Int
  ): Future[ServerBinding] =
    implicit val system = ActorSystem(Behaviors.empty, "actor-system")

    val route =
      pathPrefix("users"):
        concat(
          get:
            complete(usersService.users().toArray)
          ,
          pathPrefix(Segment / "credit"): username =>
            concat(
              get:
                usersService.checkCredit(Username(username)) match
                  case Left(value)  => complete(NotFound, "User not found")
                  case Right(value) => complete(value)
              ,
              post:
                entity(as[Credit]) { credit =>
                  usersService.rechargeCredit(Username(username), credit) match
                    case Left(value)  => complete(NotFound, "User not found")
                    case Right(value) => complete(value)

                }
            ),
          post:
            entity(as[Username]) { username =>
              usersService.registerUser(username) match
                case Left(value) =>
                  complete(Conflict, "Username already in use")
                case Right(value) => complete(value)
            }
        )

    Http().newServerAt(host, port).bind(route)
