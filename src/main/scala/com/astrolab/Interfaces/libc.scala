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

package com.astrolab.Interfaces

// Imports for JNA
import com.sun.jna.{Library, Native, Platform, Structure, Pointer}
import com.sun.jna.ptr.{IntByReference}

// Imports for Spark
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext
import org.apache.spark.SparkFiles


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
  def myarray(x: Array[Double], array_size: Int): Unit

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

// Building loader for the two libraries
object LibraryLoader {
  lazy val load_my = {
    System.load(SparkFiles.get("libmy_udf.so"))
  }
}

object HelloWorld {
  def time[R](text: String, block: => R, loops: Int = 1): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()

    var dt:Double = ((t1 - t0)/loops.toDouble).asInstanceOf[Double] / (1000.0*1000.0*1000.0)

    val unit = "s"

    println("\n" + text + "> Elapsed time:" + " " + dt + " " + unit)

    result
  }

  def test_Spark = {

    val cores = 100
    val conf = new SparkConf().setMaster("local[*]").setAppName("TSpark").
      set("spark.cores.max", s"$cores").
      set("spark.executor.memory", "200g")


    println("===== Launch a Spark pipeline that calls C functions via JNA")

    val nil: List[Double] = Nil

    val sc = new SparkContext(conf)
    val l = sc.parallelize((1 to 10)).map(x => {
      LibraryLoader.load_my; MyLibraries.my_lib.mysum(x, 12)
    }).
      map(x => {
        LibraryLoader.load_my; MyLibraries.my_lib.mymultiply(x.toDouble, 0.5)
      }).
      aggregate(nil)((x, y) => y :: x, (x, y) => y ::: x).toArray
    println(l.mkString(" "))

    println("===== Call a C function that modifies a Scala array")

    MyLibraries.my_lib.myarray(l, l.length)
    println(l.mkString(" "))
  }

  def main(args: Array[String]) {

    println("===== Calling simple functions with numeric scalars")
    val r1 = MyLibraries.my_lib.mysum(1, 2)
    val r2 = MyLibraries.my_lib.mymultiply(1.111, 2.222)
    println("r1 = " + r1.toString + " r2 = " + r2.toString)

    println("===== Comparing overhead from Scala versus C")

    time("scala cos", {
      for (i <- 0 to 100000)
      {
        val angle = 12.0
        math.cos(angle)
      }
    }, 100000)


    time("C cos", {
      for (i <- 0 to 100000)
      {
        val angle = 12.0
        MathLibraries.m.cos(angle)
      }
    }, 100000)

    if (true) test_Spark

    println("===== Call a C function that modifies a large Scala array")

    val rand = scala.util.Random

    // val a = (for (i <- 1 to 10*1000*1000) yield rand.nextDouble).toArray

    val megas = 1
    val a = (for (i <- 1 to megas * 1000 * 1000) yield rand.nextDouble).toArray

    var result: Double = 0.0

    val iterations = 100
    time("Copy Large array", {
      for (i <- 0 to iterations) {
        val b = a.clone()
        val before = b.sum

        // println(s"Apply function to an array: sum $before")
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
      MyLibraries.my_lib.myarray(b, b.length)
      val after = b.sum

      //println (s"Apply function to an array: sum $before $after")
        result += after
      }
      result
    }, iterations)

    println (s"Apply function to an array calling a C function: $result")

    val pt = new com.astrolab.Interfaces.Point.P();
    val r3 = MyLibraries.my_lib.translate(pt, 100.0, 100.0, 100.0);

    println(s"Translate a Point x=${pt.x} y=${pt.y} z=${pt.z}")

    val ptr = new IntByReference(10)
    MyLibraries.my_lib.modify(ptr)
    println(s"ptr = ${ptr.getValue}")

    val r4 = MyLibraries.my_lib.myconcat("aaaa", "bbbb")
    println(s"r4 = ${r4}")
    MyLibraries.my_lib.myfree(r4)
  }
}

