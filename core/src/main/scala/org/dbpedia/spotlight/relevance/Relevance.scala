package org.dbpedia.spotlight.relevance

import org.dbpedia.spotlight.db.model.ContextStore
import org.dbpedia.spotlight.model._
import scala.collection.mutable
import scala.collection.JavaConverters._


/**
 * Created by dav009 on 11/02/2014.
 */

trait RelevanceScore{

 // def calculateRelevances(listOfResourceOcurrence:java.util.List[DBpediaResourceOccurrence],
 //                         allText:Text):java.util.Map[DBpediaResource, java.lang.Double]

  def getRelevanceScores(textVector:Map[TokenType,Int],
                         topicContextVectors:Map[DBpediaResource,java.util.Map[TokenType, Int]],
                         topicFrequencyInText:Map[DBpediaResource,Int]
                        ):mutable.Map[DBpediaResource, Double]
}


class RelevanceScorer(contextStore: ContextStore, relevanceScore:RelevanceScore) {
  /*
  * Get the context vectors for a list of DbpediaResources
  * */
  def getContextVectors(listOfDbpediaResources:Iterable[DBpediaResource]):Map[DBpediaResource,java.util.Map[TokenType, Int]] ={
    val contextCounts = mutable.Map[DBpediaResource, java.util.Map[TokenType, Int]]()
    for(dbpediaResource<-listOfDbpediaResources){
      val currentCounts = contextStore.getContextCounts(dbpediaResource)
      contextCounts(dbpediaResource) = currentCounts
    }
    return contextCounts.toMap
  }

  /*
  * Remove stop words and return a frequency map of tokens.
  * */
  def processTextVector(textContext:List[Token]):Map[TokenType, Int]={
    val cleanedTokens = textContext.filter(token => token.tokenType!=TokenType.UNKNOWN && token.tokenType!= TokenType.STOPWORD)
    return cleanedTokens.groupBy(_.tokenType).mapValues(_.size)
  }


  def calculateRelevances(listOfResourceOcurrence:java.util.List[DBpediaResourceOccurrence],
                          allText:Text):java.util.Map[DBpediaResource,java.lang.Double]={

    val listOfDbpediaOcurrences = listOfResourceOcurrence.asScala
    val topicFrequencyInText = listOfDbpediaOcurrences.groupBy(_.resource).mapValues(_.size)

    val textVector:Map[TokenType,Int] = processTextVector(allText.featureValue("tokens").get)
    val topicContextVectors = getContextVectors(topicFrequencyInText.keys)

    val scores = relevanceScore.getRelevanceScores(textVector, topicContextVectors, topicFrequencyInText)

    return scores.asJava.asInstanceOf[java.util.Map[DBpediaResource, java.lang.Double]]
  }

}
