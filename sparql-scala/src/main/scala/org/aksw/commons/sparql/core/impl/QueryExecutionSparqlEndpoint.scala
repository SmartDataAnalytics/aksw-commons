package org.aksw.commons.sparql.core.impl

import org.aksw.commons.sparql.core.SparqlEndpoint
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.query.QueryExecution

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: 4/15/11
 * Time: 10:52 PM
 * To change this template use File | Settings | File Templates.
 */

trait QueryExecutionSparqlEndpoint
        extends SparqlEndpoint
{
  def createQueryExecution(query: String) : QueryExecution

  override def executeSelect(query: String) = createQueryExecution(query).execSelect

  override def executeAsk(query: String) = createQueryExecution(query).execAsk

  override def executeConstruct(query: String) = createQueryExecution(query).execConstruct

  override def defaultGraphNames(): Set[String] = Set()
}