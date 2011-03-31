package org.aksw.commons.sparql.core.decorator

import org.aksw.commons.sparql.core.SparqlEndpoint

/**
 * A wrapper for graphdaos which delays execution of queries
 * in order to prevent flooding.
 *
 * Created by Claus Stadler
 * Date: Oct 6, 2010
 * Time: 8:25:26 PM
 */
class DelayingSparqlEndpoint(val decoratee : SparqlEndpoint, val delay : Long)
  extends SparqlEndpoint
{
  private var lastExecutionTime = 0l;

  def doDelay() : Unit = {
    val now = System.nanoTime;
    val remainingDelay = Math.max(0l, (delay - ((now - lastExecutionTime) / 1000000.0)).longValue)

    if(remainingDelay != 0l) {
      try {
        Thread.sleep(remainingDelay)
      } catch { case e : Exception => e.printStackTrace } // Should never happen?!
    }

    lastExecutionTime = System.nanoTime
  }

  def executeSelect(query : String) = { doDelay; decoratee.executeSelect(query) }
	def executeAsk(query : String) = { doDelay; decoratee.executeAsk(query) }
	def executeConstruct(query : String) = { doDelay; decoratee.executeConstruct(query) }

	def defaultGraphNames() = decoratee.defaultGraphNames

  override def id() = decoratee.id
}