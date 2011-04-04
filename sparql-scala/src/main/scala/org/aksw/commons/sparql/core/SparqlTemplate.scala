package org.aksw.commons.sparql.core

import org.apache.velocity.app.VelocityEngine
import java.io.StringWriter
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.apache.velocity.{Template, VelocityContext}

/**
 * User: Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann 
 * Date: 04.04.11
 */

object SparqlTemplate extends Application{

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
  lazy val classesOfInstance = ve.getTemplate("sparqltemplates/classesOfInstance.vm")
  lazy val instancesOfClasses = ve.getTemplate("sparqltemplates/instancesOfClasses.vm")


  def classesOfInstance(context: VelocityContext): String = (doit(classesOfInstance, context))

  def instancesOfClasses(context: VelocityContext): String = (doit(instancesOfClasses, context))


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