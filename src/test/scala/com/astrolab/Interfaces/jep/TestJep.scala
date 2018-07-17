/*
 * Copyright 2018 Christian Arnault
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.astrolab.Interfaces.jep

import org.scalatest.{BeforeAndAfterAll, FunSuite}

import jep._
import scala.util.Random
import collection.JavaConverters._


class testJep extends FunSuite with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
  }

  test("test JEP") {
    val j = new Jep(new JepConfig())
    j.set("x", 10)
    val x = j.getValue("x")
    println(s"x = $x")
    assert(x == 10)
  }

  test("import numpy") {
    val j = new Jep(new JepConfig().addSharedModules("numpy"))
    j.eval("import numpy as np")
    assert(true)
  }

  test("Set and get a numeric value") {
    val j = new Jep(new JepConfig().addSharedModules("numpy"))
    j.set("x", 10)
    val x = j.getValue("x")
    assert(x == 10)
  }

  test("create a matrix"){
    val j = new Jep(new JepConfig().addSharedModules("numpy"))
    j.eval("y = np.random.rand(2, 3)")
    val shape = j.getValue("y.shape").asInstanceOf[java.util.List[Int]].asScala
    println(s"shape=$shape ${shape.length}")
    assert(shape.length == 2)
  }

  test("Create a large NDArray") {
    val arraySize = 1000000

    val j = new Jep(new JepConfig().addSharedModules("numpy"))
    j.set("arraySize", arraySize)
    j.eval("z = np.random.rand(arraySize)")
    val shape = j.getValue("z.shape").asInstanceOf[java.util.List[Int]].asScala
    println(s"shape = $shape")
    assert(shape.length == 1)
  }

  test ("Send a Scala array to Python") {
    val arraySize = 2000000

    val j = new Jep(new JepConfig().addSharedModules("numpy"))
    val f = Array.fill(arraySize)(Random.nextFloat)
    val nd = new NDArray[Array[Float]](f, arraySize)
    j.set("t", nd)
    j.eval("shape = t.shape")
    val shape = j.getValue("t.shape").asInstanceOf[java.util.List[Int]].asScala
    println(s"shape = $shape")
    val size = j.getValue("shape[0]")
    assert(size == arraySize)
  }
}


