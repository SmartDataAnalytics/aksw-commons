package org.aksw.commons.sparql.core




import com.hp.hpl.jena.query.{ResultSetFormatter, QuerySolution}
import org.aksw.commons.collections.scala.PrefetchIterator


class QueryResultIterator(val sparqlEndpoint : SparqlEndpoint, val query : String, val limit : Int, var offset : Int)
	extends PrefetchIterator[QuerySolution]
{
	private var isEndReached : Boolean = false;

	def buildQuery() : String = {
		var result = query;

		if(limit != 0)
			result += " LIMIT " + limit;
			
		if(offset != 0)
			result += " OFFSET " + offset;
		
		return result;
	}

  private def asSeq[T](iterable : Iterable[T]) : Seq[T] =
  {  
      val result = iterable match {
        case x : Seq[T] => x
        case _ => iterable.toSeq
      }

      return result
  }
	
	override def prefetch() : Iterator[QuerySolution] = {
		
		if(isEndReached)
			return null;
		
		val q = buildQuery();

    val rs = sparqlEndpoint.executeSelect(q)
    //val seq = asSeq(graphDAO.executeSelect(q))

		//val list = new collection.JavaConversions.JIterableWrapper(rs)
    val list =  new collection.JavaConversions.JListWrapper(ResultSetFormatter.toList(rs))
    
    //Thread.sleep(1000);

		if(offset == 0)
			offset = limit;
		else
			offset += limit;

		if(list.length < limit || limit == 0)
			isEndReached = true;

		return list.iterator;
	}
}
