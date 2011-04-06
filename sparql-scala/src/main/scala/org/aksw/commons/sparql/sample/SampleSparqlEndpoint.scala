package org.aksw.commons.sparql.core

import org.aksw.commons.sparql.core.impl.HttpSparqlEndpoint

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 * just a sample implementation
 * for your convenience ... 
 */

object SampleSparqlEndpoint {

  def main(args: Array[String]): Unit = {
    val coreEndpoint = new HttpSparqlEndpoint("http://dbpedia.org/sparql", Set("http://dbpedia.org"))  with WithoutLikeSupport


    //val r = coreEndpoint.executeSelect( "Select ?s ?p ?o From <http://dbpedia.org> { ?s ?p ?p . } Limit 10")

    println(coreEndpoint.like("test", Set("aa", "bb")))


  }
}