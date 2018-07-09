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

package com.astrolab.Interfaces.jna

import org.scalatest.{BeforeAndAfterAll, FunSuite}

// Imports for JNA
import com.sun.jna.{Library, Native, Platform, Structure, Pointer}
import com.sun.jna.ptr.{IntByReference}


/*

We have defined some C functions in src/C.
We have to declare a Scala representation for all those functions
In addition, one Java representation of the Point structure is available in java/ca/Point.java since
JNA cannot accept getFieldOrder inside a Scala (the fields must be public)
However, all functions using the Point type can be declared from Scala

 */
trait MyEntryPoints extends Library {
  def mysum(x: Int, y: Int): Int

  def mymultiply(x: Double, y: Double): Double
  def myarraymultiply(x: Array[Double], array_size: Int): Unit

  def myconcat(a: String, b: String): String;
  def myfree(a: String): Unit;

  def translate(pt: com.astrolab.Interfaces.Point.P, x: Double, y: Double, z: Double): com.astrolab.Interfaces.Point.P
  def modify(ptr: IntByReference)
}

// This object loads the shared library for all my C functions
// (refer to the Makefile to build this shared library)
object MyLibraries {
  def my_lib = Native.loadLibrary("my_udf", classOf[MyEntryPoints]).asInstanceOf[MyEntryPoints]
}

/*

This declares one entry points of the "m" system library

 */
trait MathEntryPoints extends Library {
  def cos(angle: Double): Double
}

object MathLibraries {
  def m = Native.loadLibrary("m", classOf[MathEntryPoints]).asInstanceOf[MathEntryPoints]
}

/**

  * Test class

  */

class testJNA extends FunSuite with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
  }

  val rand = scala.util.Random

  var lastTimer = ""

  def time[R](text: String, block: => R, loops: Int = 1): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()

    val dt = ((t1 - t0)/loops.toDouble).asInstanceOf[Double] / (1000.0*1000.0*1000.0)

    val unit = "s"

    lastTimer = (text + "> Elapsed time:" + " " + dt + " " + unit)

    result
  }

  test("Calling cos function from std library") {
    val r = MathLibraries.m.cos(scala.math.Pi)
    assert(r == -1.0)
  }

  test("Calling simple function with int numeric scalars") {
    // println("===== Calling simple function with int numeric scalars")
    val r = MyLibraries.my_lib.mysum(1, 2)
    assert(r == 3)
    // println(s"r = $r")
  }

  test("Calling simple function with double numeric scalars") {
    val r = MyLibraries.my_lib.mymultiply(1.111, 2.222)
    assert(r == 2.468642)
    // println(s"r = $r")
  }

  test("Comparing overhead from Scala versus C") {
    time("scala cos", {
      for (i <- 0 to 100000)
      {
        val angle = 12.0
        math.cos(angle)
      }
    }, 100000)

    val t1 = lastTimer

    var r2 = 0.0
    time("C cos", {
      for (i <- 0 to 100000)
      {
        val angle = 12.0
        MathLibraries.m.cos(angle)
      }
    }, 100000)

    val t2 = lastTimer

    assert(t1 != t2)
  }

  test("Compare Calling a C function that modifies a large Scala array vs pure Scala") {
    val megas = 1
    val a = (for (i <- 1 to megas * 1000 * 1000) yield rand.nextDouble).toArray

    var result: Double = 0.0

    val iterations = 100

    time("Copy Large array", {
      for (i <- 0 to iterations) {
        val b = a.clone()
        val before = b.sum

        result = before
      }
      result
    }, iterations)

    println(s"Copy Large array: $result")

    time("Using large array in Scala", {
      result = 0.0
      for (i <- 0 to iterations) {
        val b = a.clone()
        val before = b.sum
        val after = b.map(_ * 2.0).sum

        //println (s"Apply function to an array: sum $before $after")
        result += after
      }
      result
    }, iterations)

    println (s"Apply function to an array using Scala map: $result")

    time("Using large array from C", {
      result = 0.0
      for (i <- 0 to iterations) {
        val b = a.clone()
        val before = b.sum
        MyLibraries.my_lib.myarraymultiply(b, b.length)
        val after = b.sum

        //println (s"Apply function to an array: sum $before $after")
        result += after
      }
      result
    }, iterations)


    assert(true)
  }

  test("Translate a Point defined in a structure") {
    val pt = new com.astrolab.Interfaces.Point.P();
    val p0 = pt.clone
    val p1 = MyLibraries.my_lib.translate(pt, 100.0, 100.0, 100.0);

    // println(s"Translated Point x=${pt.x} y=${pt.y} z=${pt.z}")

    assert(p1 != p0)
  }

  test("Modify a double value passed by reference to C") {
    val ptr = new IntByReference(10)
    MyLibraries.my_lib.modify(ptr)
    println(s"ptr = ${ptr.getValue}")

    assert(true)
  }

  test("Create a C string from the two concatenated strings") {
    val r4 = MyLibraries.my_lib.myconcat("aaaa", "bbbb")
    println(s"r4 = ${r4}")
    MyLibraries.my_lib.myfree(r4)

    assert(true)
  }


  test("Call a C function that modifies a Scala array") {
    val values = Array.range(0, 1000).map(x => x.toDouble)
    val s1 = values.sum
    MyLibraries.my_lib.myarraymultiply(values, values.length)
    val s2 = values.sum
    println(values.mkString(" "))
    assert(s2 == s1*2)
  }

  /*
  test() {
  }

  */
}



