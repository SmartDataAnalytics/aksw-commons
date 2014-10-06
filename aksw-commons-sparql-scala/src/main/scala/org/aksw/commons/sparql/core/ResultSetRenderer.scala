package org.aksw.commons.sparql.core

import scalaj.collection.Imports._

import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.rdf.model.RDFNode

/**
 * User: Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 * Date: 03.04.11
 */

object ResultSetRenderer {

  /**
   * Based on the assumption, that the ResultSet only has one variable
   * will return anything as a string set
   * @return a set of URIs
   */

  def asStringSet(r: ResultSet): java.util.Set[String] = {
    require(r.getResultVars.size == 1)

    val v = r.getResultVars.get(0)
    var ret = Set[String]()

    while (r.hasNext) {
      val querySolution = r.next()
      val rdfnode: RDFNode = querySolution.get(v)
      if (rdfnode.isURIResource) {
        ret += rdfnode.asResource.getURI
      } else if (rdfnode.isLiteral) {
        ret += rdfnode.asLiteral.getLexicalForm
      }
    }

    ret.asJava
  }

}