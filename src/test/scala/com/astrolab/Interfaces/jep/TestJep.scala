
package com.astrolab.Interfaces.jep

import org.scalatest.{BeforeAndAfterAll, FunSuite}

import jep._
import scala.util.Random
import collection.JavaConverters._


class testJep extends FunSuite with BeforeAndAfterAll {

  val j = new Jep(new JepConfig().addSharedModules("numpy"))

  override protected def beforeAll(): Unit = {
    super.beforeAll()
  }

  test("import numpy") {

    j.eval("import numpy as np")
    assert(true)
  }

  test("Set and get a numeric value") {
    j.set("x", 10)
    val x = j.getValue("x")
    assert(x == 10)
  }

  test("create a matrix"){
    j.eval("y = np.random.rand(2, 3)")
    val shape = j.getValue("y.shape").asInstanceOf[java.util.List[Int]].asScala
    println(s"shape=$shape ${shape.length}")
    assert(shape.length == 2)
  }

  test("Create a large NDArray") {
    val arraySize = 1000000

    j.set("arraySize", arraySize)
    j.eval("z = np.random.rand(arraySize)")
    val shape = j.getValue("z.shape").asInstanceOf[java.util.List[Int]].asScala
    println(s"shape = $shape")
    assert(shape.length == 1)
  }

  test ("Send a Scala array to Python") {
    val arraySize = 2000000

    val f = Array.fill(arraySize)(Random.nextFloat)
    val nd = new NDArray[Array[Float]](f, arraySize)
    j.set("t", nd)
    j.eval("t2 = np.array(t)")
    j.eval("shape = t2.shape")
    val shape = j.getValue("t2.shape").asInstanceOf[java.util.List[Int]].asScala
    println(s"shape = $shape")
    val size = j.getValue("shape[0]")
    assert(size == arraySize)
  }
}


