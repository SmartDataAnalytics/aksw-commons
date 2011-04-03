package org.aksw.commons.sparql.core

import scalaj.collection.Imports._


trait Test {
  def doit(query: String): Unit

}

trait Test2 extends Test {
  abstract override def doit(query: String): Unit = synchronized(super.doit(query))

}

class AA {
  private var _age: java.util.List[String] = _

  // Getter
  def age = _age

  // Setter
  def some(x: java.util.List[String]): Unit = {
     x.asScala
    _age = x
  }


}