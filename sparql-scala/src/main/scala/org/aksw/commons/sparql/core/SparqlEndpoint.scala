package org.aksw.commons.sparql.core


import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.graph.Triple
import com.hp.hpl.jena.query.ResultSet

import scalaj.collection.Imports._
import org.apache.commons.lang.NotImplementedException

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: Sep 8, 2010
 * Time: 12:19:28 PM
 *
 * TODO The 'like' stuff does not belong here - it should go into something like an
 *      abstract SparqlQueryBuilder class with vendor specific implementations.
 *
 */

trait SparqlEndpoint {
  def executeSelect(query: String): ResultSet

  def executeAsk(query: String): Boolean

  def executeConstruct(query: String): Model
  def executeConstruct(query: String, model: Model): Model

  //def like(variable: String, patterns: String*): String = (like(variable, patterns))

  def like(variable: String, patterns: Set[String]): String = (like(variable, patterns.asJava))

  /**
   * TODO This stuff does not belong here and is subject to removal
   *
   * @return the respective like paradigm
   */
  def like(variable: String, patterns: java.util.Set[String]): String = {
    if (patterns.isEmpty) {
      return ""
    }
    val stringBuffer = new StringBuffer("FILTER ( ?" + variable + " LIKE (")

    for (str: String <- patterns.asScala) {
      stringBuffer.append("<").append(str).append("%>,")
    }

    stringBuffer.deleteCharAt(stringBuffer.length - 1)

    stringBuffer.append("))").toString
  }

  //def insert(triples: Iterable[Triple], graphName: String): Unit = { throw new NotImplementedException(); }

  //def remove(triples: Iterable[Triple], graphName: String): Unit = { throw new NotImplementedException(); }

  def insert(model: Model, graphName: String): Unit = { throw new NotImplementedException(); }

  def remove(model: Model, graphName: String): Unit = { throw new NotImplementedException(); }


  /**
   * Ideally this method returns an id uniquely identifying a specific
   * sparql endpoint
   */
  def id(): String

  /**
   * This method must never return null
   */
  def defaultGraphNames(): Set[String]

  /**
   * Tests whether the sparql endpoint is alive.
   */
  def isAlive(): Boolean = {
    try {
      executeAsk("Ask {?s a ?o}")
    }
    catch {
      case e: Exception => false
    }

    true
  }

}

trait WithoutLikeSupport extends SparqlEndpoint {
  override def like(variable: String, patterns: java.util.Set[String]): String = {
    if (patterns.isEmpty) {
      return ""
    }
    val stringBuffer = new StringBuffer("FILTER (")

    for (str: String <- patterns.asScala) {
      stringBuffer.append(" regex (str(?").append(variable).append("),\"^").append(str).append(".*\") &&")
    }

    stringBuffer.deleteCharAt(stringBuffer.length-1).deleteCharAt(stringBuffer.length-1)

    stringBuffer.append(" )").toString
  }
}