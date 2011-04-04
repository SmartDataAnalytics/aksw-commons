package org.aksw.commons.sparql.core

import org.apache.velocity.app.VelocityEngine
import java.io.StringWriter
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.apache.velocity.{Template, VelocityContext}

import scalaj.collection.Imports._

/**
 * User: Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann 
 * Date: 04.04.11
 */

class SparqlTemplate(var limit: Int, var from: String*) {
  //TODO should be a list
  def this(limit: Int) = this (limit, null)

  var filterList: java.util.List[String] = new java.util.ArrayList[String]();

  def addFilter(s: String) {
    filterList.add(s);
  }

  def getQuery(templateFile: String, velocityContext: VelocityContext): String = {
    velocityContext.put("limit", limit);
    if (from == null) {
      velocityContext.put("from", from);
    }
    if (!filterList.isEmpty) {
      velocityContext.put("filterList", filterList)
    }
    val writer = new StringWriter
    SparqlTemplate.ve.getTemplate(templateFile).merge(velocityContext, writer)
    writer.toString
  }

}

object SparqlTemplate extends Application {

  lazy val ve: VelocityEngine = {
    val tmp = new VelocityEngine
    tmp.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
    tmp.setProperty("classpath.resource.loader.class", classOf[ClasspathResourceLoader].getName)
    //TODO register these as user directives
    //tmp.setProperty("userdirective", org.apache.velocity.tools.generic.directive.Ifnull)
    //tmp.setProperty("userdirective", org.apache.velocity.tools.generic.directive.Ifnotnull)
    tmp.init
    tmp
  }

  lazy val map = Map[String, Template]("sparqltemplates/allClasses.vm" -> allClasses)

  lazy val allClasses = ve.getTemplate("sparqltemplates/allClasses.vm")

  lazy val classesOfInstance = ve.getTemplate("sparqltemplates/classesOfInstance.vm")
  lazy val instancesOfClass = ve.getTemplate("sparqltemplates/instancesOfClass.vm")


  def allClasses(context: VelocityContext): String = (doit(allClasses, context))

  def classesOfInstance(context: VelocityContext): String = (doit(classesOfInstance, context))

  def instancesOfClass(context: VelocityContext): String = (doit(instancesOfClass, context))


  def doit(t: Template, context: VelocityContext): String = {
    val writer = new StringWriter
    t.merge(context, writer)
    writer.toString
  }

  override def main(args: Array[String]): Unit = {

    val context = new VelocityContext
    context.put("name", "world")

    println(classesOfInstance(context))
    //getClasses(new VelocityContext())

  }


}