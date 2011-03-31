package org.aksw.commons.semweb.sparql.core.impl

import java.lang.String
import collection.JavaConversions.JIterableWrapper
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP
import com.hp.hpl.jena.query.{ResultSet}
import org.aksw.commons.semweb.sparql.core.SparqlEndpoint

/**
 * Created by Claus Stadler
 * User: raven
 * Date: Sep 8, 2010
 * Time: 12:26:13 PM
 *
 * A shallow convenience wrapper for Jena's QueryEngineHTTP
 */
class HttpSparqlEndpoint(val serviceName: String, override val defaultGraphNames: Set[String])
        extends SparqlEndpoint
{
  def this(serviceName: String) = this (serviceName, Set[String]())

  def this(serviceName: String, defaultGraphName: String) = this (serviceName, if (defaultGraphName == null) Set[String]() else Set(defaultGraphName))

  def this(serviceName: String, defaultGraphNames: Iterable[String]) = this (serviceName, defaultGraphNames.toSet)

  def this(serviceName: String, defaultGraphNames: java.lang.Iterable[String]) = this (serviceName, JIterableWrapper(defaultGraphNames))

  private def queryExecution(query: String): QueryEngineHTTP = {

    //println("Query is: " + query)

    val result = new QueryEngineHTTP(serviceName, query)

    defaultGraphNames.foreach(_ => result.addDefaultGraph(_))

    return result
  }

  def executeConstruct(query: String): Model = {
    return queryExecution(query).execConstruct
  }

  def executeAsk(query: String): Boolean = {
    return queryExecution(query).execAsk
  }


  def executeSelect(query: String): ResultSet = {
    return queryExecution(query).execSelect()
  }

  override def id() = serviceName

  

  /*
  def executeSelect(query: String) : Iterable[QuerySolution] = {
    /*
    val rs = queryExecution(query).execSelect()
    return rs;
    */


    val rs = queryExecution(query).execSelect()
    val tmp = ResultSetFormatter.toList(rs)
    //val result = new collection.JavaConversions.JListWrapper(tmp)

    return tmp;
  }
  */

}