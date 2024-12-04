package rides.adapters

import rides.domain.model.*
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol.*

object Marshalling:
  export spray.json.RootJsonFormat
  export spray.json.DefaultJsonProtocol.{*, given}
  export akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.{*, given}
  export akka.http.scaladsl.unmarshalling.Unmarshal
  export akka.http.scaladsl.marshalling.Marshal

  given RootJsonFormat[EBikeId] = jsonFormat1(EBikeId.apply)
