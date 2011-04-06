package org.aksw.commons.sparql.core


import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.query.ResultSet

import scalaj.collection.Imports._

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: Sep 8, 2010
 * Time: 12:19:28 PM
 */

trait SparqlEndpoint {
  def executeSelect(query: String): ResultSet

  def executeAsk(query: String): Boolean

  def executeConstruct(query: String): Model

  //def like(variable: String, patterns: String*): String = (like(variable, patterns))

  def like(variable: String, patterns: Set[String]): String = (like(variable, patterns.asJava))

  /**
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

  def insert(model: Model, graphName: String): Unit = {}

  def remove(model: Model, graphName: String): Unit = {}

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
   * used for one of tests to see if the endpoint is alive
   */
  def isAlive(): Boolean = {
    try {
      executeAsk("ASK {?s ?p ?o} ")
    }
    catch {
      case e: Exception => false
    }

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