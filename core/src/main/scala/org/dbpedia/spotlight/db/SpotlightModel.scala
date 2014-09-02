package org.dbpedia.spotlight.db

import java.util.Properties
import org.apache.commons.logging.{LogFactory, Log}
import org.dbpedia.spotlight.db.model.TextTokenizer
import org.dbpedia.spotlight.disambiguate.ParagraphDisambiguatorJ
import org.dbpedia.spotlight.exceptions.InputException
import org.dbpedia.spotlight.model.SpotlightConfiguration.DisambiguationPolicy
import org.dbpedia.spotlight.model.SpotterConfiguration.SpotterPolicy
import org.dbpedia.spotlight.model._
import org.dbpedia.spotlight.spot.Spotter
import org.dbpedia.spotlight.relevance.RelevanceScorer
import collection.JavaConversions._
import org.dbpedia.spotlight.db.memory.MemoryContextStore

/**
 * Created by David Przybilla on 19/08/2014.
 */


trait SpotlightModel{

  val tokenizer: TextTokenizer
  val spotters: java.util.Map[SpotterPolicy, Spotter]
  val disambiguators: java.util.Map[DisambiguationPolicy, ParagraphDisambiguatorJ]
  val properties: Properties
  val relevanceScorer: Option[RelevanceScorer]

  val LOG: Log = LogFactory.getLog(this.getClass)

  def spot(text: Text, params: AnnotationParameters): java.util.List[SurfaceFormOccurrence];

  @throws( classOf[InputException] )
  def firstBest(text: String, params: AnnotationParameters): java.util.List[DBpediaResourceOccurrence]

  def nBest(stringText: String, params: AnnotationParameters,n: Int ): java.util.Map[SurfaceFormOccurrence, java.util.List[DBpediaResourceOccurrence]]

  def getSpotter(name: String): Spotter = {
    var policy: SpotterConfiguration.SpotterPolicy = SpotterPolicy.Default
    try {
      policy = SpotterPolicy.valueOf(name)
    }
    catch {
      case e: IllegalArgumentException => {
        throw new InputException(String.format("Specified parameter spotter=%s is invalid. Use one of %s.", name, SpotterPolicy.values))
      }
    }


    if (spotters.size == 0) throw new InputException(String.format("No spotters were loaded. Please add one of %s.", spotters.keySet))
    val spotter: Spotter = spotters.get(policy)
    if (spotter == null) {
      throw new InputException(String.format("Specified spotter=%s has not been loaded. Use one of %s.", name, spotters.keySet))

      }
    return spotter
    }


  def getDisambiguator(name: String): ParagraphDisambiguatorJ = {
    var policy: SpotlightConfiguration.DisambiguationPolicy = DisambiguationPolicy.Default
    try {
      policy = DisambiguationPolicy.valueOf(name)
    }
    catch {
      case e: IllegalArgumentException => {
        throw new InputException(String.format("Specified parameter disambiguator=%s is invalid. Use one of %s.", name, DisambiguationPolicy.values))
      }
    }
    if (disambiguators.size == 0) throw new InputException(String.format("No disambiguators were loaded. Please add one of %s.", disambiguators.keySet))
    val disambiguator: ParagraphDisambiguatorJ = disambiguators.get(policy)
    if (disambiguator == null) throw new InputException(String.format("Specified disambiguator=%s has not been loaded. Use one of %s.", name, disambiguators.keySet))
    return disambiguator
  }

  def getRelevances(listOfResourceOcurrence:java.util.List[DBpediaResourceOccurrence]):java.util.List[DBpediaResourceOccurrence]= {
    val allText:Text = listOfResourceOcurrence.get(0).context
    if(this.tokenizer != null){
      this.tokenizer.tokenizeMaybe(allText);
    }
    val relevanceScores:java.util.Map[DBpediaResource, java.lang.Double] = this.relevanceScorer.get.calculateRelevances(listOfResourceOcurrence, allText);
    listOfResourceOcurrence.foreach(resourceOcc => resourceOcc.setRelevanceScore(relevanceScores.get(resourceOcc.resource)))
    return listOfResourceOcurrence;
  }
}
