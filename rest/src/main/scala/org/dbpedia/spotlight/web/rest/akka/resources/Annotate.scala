package org.dbpedia.spotlight.web.rest.akka.resources

import org.dbpedia.spotlight.model.SpotlightConfiguration
import org.dbpedia.spotlight.web.rest.akka.ServerExample._
import org.dbpedia.spotlight.web.rest.akka.output.Spot
import org.dbpedia.spotlight.web.rest.akka.output.Resource
import reflect.ClassTag
import scala.concurrent.{ExecutionContext, Future}
import org.dbpedia.spotlight.web.rest.akka.output.SpotlightMarshallers._

object Annotate {

  def endpoint(implicit context:ExecutionContext) = parameters("text", 'confidence.as[Double]?, 'url?,
    'support.as[Int]?,'types?, 'sparqlQuery?, 'policy?,
    'coreferenceResolution.as[Boolean]?, 'spotter?, 'disambiguator?) {
    (text, InputConfidence, inputUrl, inputSupport, inputTypes, inputSparqlQuery, inputPolicy,
     inputCoreferenceResolution, inputSpotter, inputDisambiguator) =>
      complete{
        Future {
          val confidence = InputConfidence.getOrElse(SpotlightConfiguration.DEFAULT_CONFIDENCE)
          val url = InputUrl.getOrElse(SpotlightConfiguration.DEFAULT_URL)
          val support = InputSupport.getOrElse(SpotlightConfiguration.DEFAULT_SUPPORT)
          val types = inputTypes.getOrElse(SpotlightConfiguration.DEFAULT_TYPES)
          val sparqlQuery = inputSparqlQuery.getOrElse(SpotlightConfiguration.DEFAULT_SPARQL)
          val policy = inputPolicy.getOrElse(SpotlightConfiguration.DEFAULT_POLICY)
          val coreferenceResolution = inputCoreferenceResolution.getOrElse(SpotlightConfiguration.DEFAULT_COREFERENCE_RESOLUTION)
          val spotter = inputSpotter.getOrElse("Default")
          val disambiguator = inputDisambiguator.getOrElse("Default")

          new Spot("name", 10, "type",  Array())

          // Extractor.annotate(text, confidence.getOrElse("0.5"))
        }
      }
  }
}
