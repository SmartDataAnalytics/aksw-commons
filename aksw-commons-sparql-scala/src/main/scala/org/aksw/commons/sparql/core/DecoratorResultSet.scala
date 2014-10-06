package org.aksw.commons.sparql.core

import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.sparql.engine.binding.Binding
import java.util.List

/**
 * Created by Claus Stadler
 * Date: Oct 21, 2010
 * Time: 9:57:03 PM
 */
abstract class DecoratorResultSet(decoratee: ResultSet) extends ResultSet {

  def hasNext: Boolean = decoratee.hasNext

  def next: QuerySolution = decoratee.next

  def remove: Unit = decoratee.remove

  def nextSolution: QuerySolution = decoratee.nextSolution

  def nextBinding: Binding = decoratee.nextBinding

  def getRowNumber: Int = decoratee.getRowNumber

  def getResultVars: List[String] = decoratee.getResultVars

  def getResourceModel: Model = decoratee.getResourceModel

}