package org.dbpedia.spotlight.db

import org.dbpedia.spotlight.model.{TokenType, Token, Text}

/**
 * Copyright 2014 Idio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author David Przybilla david.przybilla@idioplatform.com
 **/
object SurfaceFormCleaner {



   val setOfBadWords = scala.collection.immutable.HashSet[String]("the","a")

   val FAKE_TOKEN_NAME = "FAKE_TOKEN__"

   def clean(sentence: List[Token]): List[Token] = {
     sentence.map{ token =>
                if (!setOfBadWords.contains(token.token.toLowerCase )){
                  token
                }else{
                  val newTokenType = new TokenType(-1000, FAKE_TOKEN_NAME, 0)
                  new Token(token.token, token.offset, newTokenType)
                }
     }
   }

}
