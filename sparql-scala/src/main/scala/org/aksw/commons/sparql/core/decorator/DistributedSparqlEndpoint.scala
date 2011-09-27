package org.aksw.commons.sparql.core.decorator

import org.aksw.commons.sparql.core.SparqlEndpoint
import org.apache.commons.lang.NotImplementedException
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.graph.Triple

/**
 * @author Claus Stadler
 *
 * Date: 7/11/11
 * Time: 5:29 PM
 */
class DistributedSparqlEndpoint(override val id : String, tripleHash : (Triple=>Int), endpointMap : (Int=>SparqlEndpoint))
  extends SparqlEndpoint
{
  def executeSelect(query : String) = { throw new NotImplementedException() }
	def executeAsk(query : String) = { throw new NotImplementedException() }
	def executeConstruct(query : String) = { throw new NotImplementedException() }
  def executeConstruct(query : String, model: Model) = { throw new NotImplementedException() }

	def defaultGraphNames() = Set()

  /*
  override def insert(triples: Iterable[Triple], graphName: String): Unit = {
    triples.foreach(triple => {
      val hash = tripleHash(triple);
      val endpoint = endpointMap(hash);

      // TODO Batch the inserts
      endpoint.insert(Set(triple), graphName);
    })
  }

  override def remove(triples: Iterable[Triple], graphName: String): Unit = { throw new NotImplementedException(); }
  */

  //override def id() = id
}