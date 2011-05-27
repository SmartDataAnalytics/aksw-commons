package org.aksw.commons.sparql.core.impl

import java.lang.String
import collection.JavaConversions.JIterableWrapper
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP
import com.hp.hpl.jena.query.ResultSet
import org.aksw.commons.sparql.core.SparqlEndpoint
import org.slf4j.{LoggerFactory, Logger}

/**
 * Created by Claus Stadler
 * User: raven
 * Date: Sep 8, 2010
 * Time: 12:26:13 PM
 *
 * A shallow convenience wrapper for Jena's QueryEngineHTTP
 */

object HttpSparqlEndpoint {
  val logger: Logger = LoggerFactory.getLogger(classOf[HttpSparqlEndpoint])
}

class HttpSparqlEndpoint(val serviceName: String, override val defaultGraphNames: Set[String])
  extends QueryExecutionSparqlEndpoint {
  def this(serviceName: String) = this (serviceName, Set[String]())

  def this(serviceName: String, defaultGraphName: String) = this (serviceName, if (defaultGraphName == null) Set[String]() else Set(defaultGraphName))

  def this(serviceName: String, defaultGraphNames: Iterable[String]) = this (serviceName, defaultGraphNames.toSet)

  def this(serviceName: String, defaultGraphNames: java.lang.Iterable[String]) = this (serviceName, JIterableWrapper(defaultGraphNames))

  def createQueryExecution(query: String): QueryEngineHTTP = {

    HttpSparqlEndpoint.logger.trace("Executing query: " + query)
    //println("Query is: " + query)

    val result = new QueryEngineHTTP(serviceName, query)

    defaultGraphNames.foreach(_ => result.addDefaultGraph(_))

    return result
  }

  override def id() = serviceName
}
