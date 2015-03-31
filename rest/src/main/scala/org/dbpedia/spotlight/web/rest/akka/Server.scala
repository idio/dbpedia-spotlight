package org.dbpedia.spotlight.web.rest.akka

import org.dbpedia.spotlight.web.rest.akka.output.SpotlightMarshallers._
import akka.actor.{Actor, ActorSystem}
import org.dbpedia.spotlight.web.rest.akka.output.{Spot, SpotlightMarshallers}
import org.json4s.native.Serialization._
import org.json4s.{DefaultFormats, Formats}
import spray.httpx.marshalling.{ToResponseMarshaller, Marshaller}
import spray.routing.{RequestContext, SimpleRoutingApp}
import akka.pattern.ask
import akka.actor.ActorRef
import scala.concurrent.{Future, ExecutionContext}
import spray.routing.Directives
import akka.actor.{Props, Actor, ActorSystem}
import spray.routing._
import spray.http.{HttpEntity, MediaTypes}
import akka.pattern.ask
import akka.util.Timeout
import Marshaller._
//import SpotlightMarshallers._

object ServerExample extends App with SimpleRoutingApp {

  implicit val actorSystem = ActorSystem()
  import actorSystem.dispatcher




  def annotate = parameters("text", "confidence"?) {
    (text, confidence) =>
      complete{
        Future {
           new Spot("name", 10, "type",  Array())
         // Extractor.annotate(text, confidence.getOrElse("0.5"))
        }
      }
  }

  val annotateRoute = {
    (get | post) {
      path("annotate"){
        annotate
      }
    }
  }


  startServer(interface= "localhost", port=8080){
    annotateRoute
  }


}