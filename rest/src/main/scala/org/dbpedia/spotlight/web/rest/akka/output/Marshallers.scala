package org.dbpedia.spotlight.web.rest.akka.output


import org.dbpedia.spotlight.web.rest.akka.output.OutputResult
import spray.http.{HttpEntity, MediaTypes}
import spray.httpx.marshalling.Marshaller
import spray.httpx.marshalling._
import org.json4s.native.Serialization._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.Xml.{toJson, toXml}

object SpotlightMarshallers {

  implicit def json4sFormats: Formats = DefaultFormats

  implicit def resultMarshallerJson[T <: OutputResult] =
    Marshaller.of[T](MediaTypes.`application/json`) { (value, contentType, ctx) =>
      val string =  write(value)
      ctx.marshalTo(HttpEntity(contentType, string))
    }

  implicit def resultMarshaller[T <: OutputResult]: ToResponseMarshaller[T] =
    ToResponseMarshaller.oneOf(MediaTypes.`application/json`)  (resultMarshallerJson)
}
