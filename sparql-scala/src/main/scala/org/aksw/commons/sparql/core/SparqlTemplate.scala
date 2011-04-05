package org.aksw.commons.sparql.core

import org.apache.velocity.app.VelocityEngine
import java.io.StringWriter
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.apache.velocity.{Template, VelocityContext}

import java.util.ArrayList

/**
 * User: Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann 
 * Date: 04.04.11
 */

class SparqlTemplate(val template: Template) {

  def this(classpathFile: String) = this (SparqlTemplate.ve.getTemplate(classpathFile))

  var limit = 1000
  var from = new java.util.HashSet[String]()
  var filter = new java.util.HashSet[String]()
  val velocityContext = new VelocityContext
  var usePrefixes = false

  def setLimit(i: Int) {
    limit = i
  }

  def setUsePrefixes(b:Boolean) = (usePrefixes =b)

  def addFrom(s: String) {
    from.add(s)
  }

  def addFrom(s: java.util.Collection[String]) {
    from.addAll(s)
  }

  def addFilter(s: String) {
    filter.add(s)
  }

  def addFilter(s: java.util.Collection[String]) {
    filter.addAll(s)
  }

  def getVelocityContext : VelocityContext = (velocityContext)

  def getQuery(): String = {

    velocityContext.put("limit", limit);

    if (!from.isEmpty) {
      velocityContext.put("fromList", new ArrayList[String](from));
    }
    if (!filter.isEmpty) {
      velocityContext.put("filterList", new ArrayList[String](filter))
    }

    if(usePrefixes){
      velocityContext.put("prefix", usePrefixes)
    }

    val writer = new StringWriter
    template.merge(velocityContext, writer)
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

  /*lazy val map = Map[String, Template]("org.aksw.commons.sparqltemplates/allClasses.vm" -> allClasses)

  lazy val allClasses = ve.getTemplate("org.aksw.commons.sparqltemplates/allClasses.vm")

  lazy val classesOfInstance = ve.getTemplate("org.aksw.commons.sparqltemplates/classesOfInstance.vm")
  lazy val instancesOfClass = ve.getTemplate("org.aksw.commons.sparqltemplates/instancesOfClass.vm")


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
         */

}