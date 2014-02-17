package org.dbpedia.spotlight.relevance

import org.junit.Test
import org.junit.Assert._
import org.dbpedia.spotlight.model.{TokenType, DBpediaResource, OntologyType}
import org.scalatest.mock.MockitoSugar.mock
import org.dbpedia.spotlight.db.model.ContextStore
import org.mockito.Mockito
import java.util
import org.scalatest.junit.AssertionsForJUnit


/**
 * Tests for the relevance scorer class.
 *
 * @author Thiago Galery
 */
class RelevanceScorerTest extends AssertionsForJUnit {

  /** Tests whether the context vectors are returned with the appropriate counts */
  @Test
  def testGetContextVectors{

    // Let's mock up some dbpedia resources.
    val barack = new DBpediaResource("http://dbpedia.org/resource/Barack_Obama",
      567,
      0.9,
      List[OntologyType]()
    )
    val marilyn = new DBpediaResource("http://dbpedia.org/resource/Marilyn_Monroe",
      675,
      0.7,
      List[OntologyType]()
    )
    // Add entities to a resource list
    val resourceList =  List[DBpediaResource](marilyn, barack)

    // Let's mock up some token type-counts associations.
    val p = new util.HashMap[TokenType, Int]
    p.put(mock[TokenType],4)
    val s = new util.HashMap[TokenType, Int]
    s.put(mock[TokenType],5)

    // Mock the context store
    val contextStore = mock[ContextStore]
    // Monkeypatch the behaviour of the context store
    Mockito.when(contextStore.getContextCounts(barack)).thenReturn(p)
    Mockito.when(contextStore.getContextCounts(marilyn)).thenReturn(s)
    // Create an instance of the scorer
    val relevance = new BaseRelevanceScore()
    val scorer = new RelevanceScorer(contextStore, relevance)
    // Run the method on the mocked up data
    val result = scorer.getContextVectors(resourceList)
    // Expect mocked up objets to appear as keys in the result
    assertTrue(result.contains(barack))
    assertTrue(result.contains(marilyn))
    // Expect Barack to have a TokenType with 4 occurences, but not 5
    assertTrue(result.get(barack).get.containsValue(4))
    assertFalse(result.get(barack).get.containsValue(5))
    // Expect Marilyn to have a TokenType with 5 occurences, but not 4
    assertFalse(result.get(marilyn).get.containsValue(4))
    assertTrue(result.get(marilyn).get.containsValue(5))

  }


}