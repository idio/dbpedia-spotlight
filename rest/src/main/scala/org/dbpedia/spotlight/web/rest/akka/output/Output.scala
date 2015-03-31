package org.dbpedia.spotlight.web.rest.akka.output

case class Annotation(text:String, spot:Array[Spot])

case class Resource(label:String, uri:String, contextualScore:Double,
                    percentageOfSecondRank:Double, support:Int,
                    priorScore:Int, finalScore:Double, types:String)

case class Spot(name:String, offset:Int, nerType:String, resources:Array[Resource])

