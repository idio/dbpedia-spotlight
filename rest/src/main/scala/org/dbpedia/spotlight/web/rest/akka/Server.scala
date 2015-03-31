package org.dbpedia.spotlight.web.rest.akka

import org.dbpedia.spotlight.web.rest.akka.output.SpotlightMarshallers._
import akka.actor.{Actor, ActorSystem}
import org.dbpedia.spotlight.web.rest.akka.output.{Spot, SpotlightMarshallers}
import org.dbpedia.spotlight.web.rest.akka.resources.Annotate
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

object ServerExample extends App with SimpleRoutingApp {

  implicit val actorSystem = ActorSystem()
  import actorSystem.dispatcher

  val annotateRoute = {
    (get | post) {
      path("annotate"){
        Annotate.endpoint
      }
    }
  }

  startServer(interface= "localhost", port=8080){
    annotateRoute
  }


}