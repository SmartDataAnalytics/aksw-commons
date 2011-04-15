package org.aksw.commons.sparql.core.impl

import org.aksw.commons.sparql.core.SparqlEndpoint
import com.hp.hpl.jena.rdf.model.{Model, ModelFactory}
import com.hp.hpl.jena.query.{QueryExecution, QueryExecutionFactory}

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: 4/15/11
 * Time: 10:43 PM
 * To change this template use File | Settings | File Templates.
 */

class ModelSparqlEndpoint(val model : Model)
        extends QueryExecutionSparqlEndpoint
{
  def this() = this (ModelFactory.createDefaultModel)

  override def createQueryExecution(query: String): QueryExecution = QueryExecutionFactory.create(query, model)

  override def defaultGraphNames(): Set[String] = Set()

  override def id() = "ModelSparqlEndpoint_" + model.hashCode

  override def insert(m : Model, graphName: String): Unit = model.add(m)

  override def remove(m: Model, graphName: String): Unit = model.remove(m)
}