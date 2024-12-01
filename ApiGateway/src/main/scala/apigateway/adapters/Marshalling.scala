package apigateway.adapters

import apigateway.domain.model.*
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol.*

object Marshalling:
  export spray.json.RootJsonFormat
  export spray.json.DefaultJsonProtocol.{*, given}
  export akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.{*, given}
  export akka.http.scaladsl.unmarshalling.Unmarshal
  export akka.http.scaladsl.marshalling.Marshal

  given RootJsonFormat[V2D] = jsonFormat2(V2D.apply)
  given RootJsonFormat[EBikeId] = jsonFormat1(EBikeId.apply)
  given RootJsonFormat[EBike] = jsonFormat4(EBike.apply)
  given RootJsonFormat[RegisterEBikeDTO] = jsonFormat3(RegisterEBikeDTO.apply)
