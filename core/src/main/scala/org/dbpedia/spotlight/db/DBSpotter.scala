package org.dbpedia.spotlight.db

import model.{TextTokenizer, SurfaceFormStore}
import org.dbpedia.spotlight.spot.Spotter
import breeze.linalg.DenseVector
import org.dbpedia.spotlight.model._
import util.control.Breaks._
import scala.{None, Some}
import org.dbpedia.spotlight.exceptions.SurfaceFormNotFoundException
import collection.mutable.ListBuffer
import opennlp.tools.util.Span
import opennlp.tools.namefind.RegexNameFinder
import java.util.regex.Pattern
import org.apache.commons.lang.StringUtils
import org.dbpedia.spotlight.log.SpotlightLog

abstract class DBSpotter(
 surfaceFormStore: SurfaceFormStore,
 spotFeatureWeights: Option[Seq[Double]],
 stopwords: Set[String]
) extends Spotter {

  var tokenizer: TextTokenizer = null

  val uppercaseFinder = new RegexNameFinder(
    Array[Pattern](
      Pattern.compile("([A-Z][^ ,!?.:;]*[ ]?)+")
    )
  )

  def findUppercaseSequences(tokens: Array[String]) = uppercaseFinder.find(tokens).map{ s: Span => new Span(s.getStart, s.getEnd, "Capital_Sequences") }.toArray

  val spotFeatureWeightVector: Option[DenseVector[Double]] = spotFeatureWeights match {
    case Some(w) => Some(DenseVector(w.toArray:_*))
    case None => None
  }

  def generateCandidates(sentence: List[Token]): Seq[Span]

  val MIN_CONFIDENCE = 0.1

  def extract(text: Text): java.util.List[SurfaceFormOccurrence] = {

    if (tokenizer != null)
      tokenizer.tokenizeMaybe(text)

    var spots = ListBuffer[SurfaceFormOccurrence]()
    val sentences: List[List[Token]] = DBSpotter.tokensToSentences(text.featureValue[List[Token]]("tokens").get)

    //Go through all sentences
    sentences.foreach{ sentence: List[Token] =>
      val spans = generateCandidates(sentence)

      println("")
      println("SPANS")
      println(spans)

      val tokenTypes = sentence.map(_.tokenType).toArray

      spans.sorted
        .foreach(chunkSpan => {
        breakable {

          val firstToken = chunkSpan.getStart
          val lastToken = chunkSpan.getEnd-1

          val tokenSeqs = ListBuffer[(Int, Int)]()

          //Taking away a left member in each step, look for the longest sub-chunk in the SF dictionary
          (firstToken to lastToken).foreach{ startToken =>
            tokenSeqs += Pair(startToken, lastToken)
          }

          //Then, do the same in the other direction:
          (firstToken to lastToken).reverse.foreach{ endToken =>
            tokenSeqs += Pair(firstToken, endToken)

          }


          println("TOKEN SEQS")
          tokenSeqs.foreach(println)

          tokenSeqs.foreach{
            case (startToken: Int, endToken: Int) => {
              val startOffset = sentence(startToken).offset
              val endOffset = sentence(endToken).offset + sentence(endToken).token.length

              val spot = text.text.substring(startOffset, endOffset)
              println("SPOT: "+ spot)



              //SpotlightLog.info(this.getClass, spot + ":" + chunkSpan.getType)


              val confidence = text.featureValue[Double]("confidence").getOrElse(0.5)
              val sfMatches = surfaceFormMatch(spot, confidence=math.max(MIN_CONFIDENCE, confidence))




              SpotlightLog.debug(this.getClass, "type:"+chunkSpan.getType)
              if (sfMatches.isDefined) {

                var spotOcc = None: Option[SurfaceFormOccurrence]

                sfMatches.get.foreach{ case (sf: SurfaceForm, score: Double) =>
                   if( spotOcc.isDefined ){
                     spotOcc.get.addCandidate(sf, text, startOffset, Provenance.Annotation, score)
                   }
                   else{
                     spotOcc = Some(new SurfaceFormOccurrence(sf, text, startOffset, Provenance.Annotation, score))
                   }

                }

                //The sub-chunk is in the dictionary, finish the processing of this chunk
                //val spotOcc = new SurfaceFormOccurrence(surfaceForm, text, startOffset, Provenance.Annotation, score)
                spotOcc.get.setFeature(new Nominal("spot_type", chunkSpan.getType))
                spotOcc.get.setFeature(new Feature("token_types", tokenTypes.slice(startToken, lastToken)))
                spots += spotOcc.get
                break()
              }
            }
          }
        }
      })
    }

    dropOverlappingSpots(spots)
  }


  /**
   * This is the most important method in this class. Given the set of possible matches,
   * which are very general (e.g. based on stems in FSASpotter), we need to find a score
   * for each match. Matches will be filtered out by this score.
   *
   * @param spot
   * @return
   */
  private def spotScore(spot: String): Option[Seq[(SurfaceForm, Double)]] = {
    try {

      spotFeatureWeightVector match {
        case Some(weights) => {


          val tokens = tokenizer.tokenize(new Text(spot))
          val stemmedSpot = SurfaceFormCleaner.getStemmedVersion(tokens)
          println("===========================")
          println("spot: "+ spot)
          println("stemmed spot: "+ stemmedSpot)

          // ignore the Main SF Store
          // read from the stem store
          val rankedCandidates = surfaceFormStore.getRankedSurfaceFormCandidates(stemmedSpot)

          // if list is empty  rise exception
          if (rankedCandidates.size < 1){
            None
          }


          // rescoring based on spot features
          val rerankedScores = rankedCandidates.map{
          case (sf: SurfaceForm, p: Double)=>
              (sf, weights dot DBSpotter.spotFeatures(sf.name, p) )
           }

          //propagate the matched surface forms
          Some(rerankedScores)
       }}

    } catch {
      case e: Exception =>{
          println("exception :" )
          e.printStackTrace()
          None
      }
    }
  }

  protected def surfaceFormMatch(spot: String, confidence: Double): Option[Seq[(SurfaceForm, Double)]] = {
    println("Starting surfaceForm Match")
    val scores: Option[Seq[(SurfaceForm, Double)]] = spotScore(spot)

    scores match {


      case Some(seqOfScores) => SpotlightLog.debug(this.getClass,
        seqOfScores.map{
          case (sf: SurfaceForm, prob: Double) =>
            sf.name+" "+prob.toString
        }.mkString(" ")
      )

       case None => SpotlightLog.debug(this.getClass, "None :" + spot)

    }


      var sfConfidenceThreshold = 0.25
      if (spotFeatureWeightVector.isDefined){
        sfConfidenceThreshold = confidence
      }
      // filter matched SurfaceForms
      scores match{
        case Some(seqOfScores) =>{
          Some(seqOfScores.filter(_._2 >= confidence))
        }
        case None => None
      }
  }


  def typeOrder: Array[String]

  /**
   * This method resolves overlap conflicts in spots by considering their source (e.g. NER, chunking) and
   * their scores.
   *
   * @param spots
   * @return
   */
  def dropOverlappingSpots(spots: Seq[SurfaceFormOccurrence]): java.util.LinkedList[SurfaceFormOccurrence] = {

    val sortedSpots = spots.distinct.sortBy(sf => (sf.textOffset, sf.surfaceForm.name.length) )

    var remove = Set[Int]()
    var lastSpot: SurfaceFormOccurrence = null

    var i = 0
    while (i < sortedSpots.size) {

      val spot = sortedSpots(i)

      if (lastSpot != null && lastSpot.intersects(spot)) {

        val spotHasBetterType = typeOrder.indexOf(spot.featureValue[String]("spot_type")) < typeOrder.indexOf(lastSpot.featureValue[String]("spot_type"))
        val spotIsLonger = spot.surfaceForm.name.length > lastSpot.surfaceForm.name.length

        if(spotIsLonger && spot.spotProb > lastSpot.spotProb/2.0) {
          remove += i-1
          lastSpot = spot
        } else if(!spotIsLonger && !(spot.spotProb > lastSpot.spotProb*2.0)) {
          remove += i
          lastSpot = lastSpot
        } else if(spot.spotProb == lastSpot.spotProb && spotHasBetterType) {
          remove += i-1
          lastSpot = spot
        } else if (spot.spotProb == lastSpot.spotProb && !spotHasBetterType) {
          remove += i
          lastSpot = lastSpot
        } else if(spot.spotProb > lastSpot.spotProb) {
          remove += i-1
          lastSpot = spot
        } else {
          remove += i
          lastSpot = lastSpot
        }
      } else {
        lastSpot = spot
      }

      i += 1
    }

    //This is super inefficient :(
    val list = new java.util.LinkedList[SurfaceFormOccurrence]()
    sortedSpots.zipWithIndex.foreach{ case (s: SurfaceFormOccurrence, i: Int) =>
      if(!remove.contains(i))
        list.add(s)
    }
    list
  }

}

object DBSpotter {
  def spotFeatures(spot: String, spotProbability: Double): DenseVector[Double] =
    DenseVector(
      //Annotation probability:
      spotProbability,

      //Abbreviations:
      if(spot.toUpperCase.equals(spot) && spot.size < 5 && !spot.matches("[0-9]+")) 1.0 else 0.0,

      //Numbers (e.g. years):
      if(spot.matches("[0-9]+")) 1.0 else 0.0,

      //Bias:
      1.0
    )

  def tokensToSentences(allTokens: List[Token]): List[List[Token]] = {

    val sentences = ListBuffer[List[Token]]()
    val sentence = ListBuffer[Token]()

    allTokens foreach { token: Token =>
      sentence.append(token)

      token.feature("end-of-sentence") match {
        case Some(b) => {
          sentences.append(sentence.toList)
          sentence.clear()
        }
        case None =>
      }
    }

    sentences.toList
  }
}
