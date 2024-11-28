package ebikes

import java.io.File
import scala.util.Try
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

object Main extends App:
  val dbDir = File("/data/db")
  val host = sys.env.get("HOST").getOrElse("0.0.0.0")
  val port = (for
    portString <- sys.env.get("PORT")
    portInt <- Try(Option(portString.toInt)).getOrElse({
      sys.error("PORT must be an integer"); None
    })
  yield (portInt)).getOrElse(8080)
  EBikes.run(dbDir, host, port)(using
    ActorSystem(Behaviors.empty, "actor-system")
  )
