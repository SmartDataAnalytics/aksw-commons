package org.aksw.commons.semweb.sparql.core


import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.query.{ResultSet, QuerySolution}

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: Sep 8, 2010
 * Time: 12:19:28 PM
 * To change this template use File | Settings | File Templates.
 */

trait ISparqlEndpoint
{
  def executeSelect(query : String) : ResultSet
  //def executeSelect(query : String) : Seq[QuerySolution]
	def executeAsk(query : String) : Boolean
	def executeConstruct(query : String) : Model


	def insert(model : Model, graphName : String) : Unit = {}
	def remove(model : Model, graphName : String) : Unit = {}

  /**
   * Ideally this method returns an id uniquely identifying a specific
   * sparql endpoint
   *
   */
  def id() : String

  // This method must never return null 
	def defaultGraphNames() : Set[String]
}