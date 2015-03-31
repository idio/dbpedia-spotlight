package org.dbpedia.spotlight.web.rest.akka.output

abstract class OutputResult

case class Annotation(text:String, spot:Array[Spot]) extends OutputResult

case class Resource(label:String, uri:String, contextualScore:Double,
                    percentageOfSecondRank:Double, support:Int,
                    priorScore:Int, finalScore:Double, types:String) extends OutputResult

case class Spot(name:String, offset:Int, nerType:String, resources:Array[Resource]) extends OutputResult

