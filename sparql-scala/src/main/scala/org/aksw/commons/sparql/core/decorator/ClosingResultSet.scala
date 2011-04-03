package org.aksw.commons.sparql.core.decorator

import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.sparql.engine.binding.Binding
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import org.aksw.commons.sparql.core.DecoratorResultSet

/**
 * Created by Claus Stadler
 * Date: Oct 21, 2010
 * Time: 8:39:43 PM
 *
 * Due to "Too many open files" problems with ResultSetFactory.fromXml(InputStream in)
 * I assume the default jena implementation doesn't close the stream
 * properly. For bugtracking I provide this implementation which
 * closes the stream as soon as the last element is fetched.
 *
 * Update: same day, 9:53:00 PM: Seems as if Jena not closing the stream
 * was the problem. Wrapping a result set and its underlying input stream
 * with this class fixed the problem.
 *
 */
object ClosingResultSet {
  protected final val logger: Logger = LoggerFactory.getLogger(classOf[ClosingResultSet])

  def apply(decoratee: ResultSet, in: InputStream) = {
    val crs = new ClosingResultSet(decoratee, in)
    crs.checkClose
    crs
  }
}

class ClosingResultSet(private val decoratee: ResultSet, private val in: InputStream) extends DecoratorResultSet(decoratee) {

  var isClosed = false


  protected def checkClose: Boolean = {
    if (!isClosed && !decoratee.hasNext) {
      try {
        isClosed = true
        in.close
      }
      catch {
        case e: Exception => {
          ClosingResultSet.logger.error("Error closing an InputStream supposedly underlying a Jena ResultSet", e)
        }
      }
    }
    return isClosed
  }

  override def hasNext: Boolean = {
    return !checkClose
  }

  override def remove: Unit = {
    super.remove
    checkClose
  }

  override def nextSolution: QuerySolution = {
    var result: QuerySolution = super.nextSolution
    checkClose
    result
  }

  override def nextBinding: Binding = {
    var result: Binding = super.nextBinding
    checkClose
    result
  }

}