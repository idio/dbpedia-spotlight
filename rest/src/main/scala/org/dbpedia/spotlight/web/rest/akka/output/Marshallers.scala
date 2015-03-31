package org.dbpedia.spotlight.web.rest.akka.output


import spray.http.{HttpEntity, MediaTypes}
import spray.httpx.marshalling.Marshaller
import spray.httpx.marshalling._
import org.json4s.native.Serialization._
import org.json4s.{DefaultFormats, Formats}
import org.json4s.Xml.{toJson, toXml}

object SpotlightMarshallers {

  implicit def json4sFormats: Formats = DefaultFormats


  val ResourceMarshallerJson =
    Marshaller.of[Resource](MediaTypes.`application/json`) { (value, contentType, ctx) =>
      val string =  write(value)
      ctx.marshalTo(HttpEntity(contentType, string))
    }

  val SpotMarshallerJson =
    Marshaller.of[Spot](MediaTypes.`application/json`) { (value, contentType, ctx) =>
      val string =  write(value)
      ctx.marshalTo(HttpEntity(contentType, string))
    }

  val AnnotationMarshallerJson =
    Marshaller.of[Annotation](MediaTypes.`application/json`) { (value, contentType, ctx) =>
      val string =  write(value)
      ctx.marshalTo(HttpEntity(contentType, string))
    }

  implicit val AnnotationMarshaller: ToResponseMarshaller[Annotation] =
    ToResponseMarshaller.oneOf(MediaTypes.`application/json`)  (AnnotationMarshallerJson)

  implicit val ResourceMarshaller: ToResponseMarshaller[Resource] =
    ToResponseMarshaller.oneOf(MediaTypes.`application/json`)  (ResourceMarshallerJson)

  implicit val SpotMarshaller: ToResponseMarshaller[Spot] =
    ToResponseMarshaller.oneOf(MediaTypes.`application/json`)  (SpotMarshallerJson)


}
