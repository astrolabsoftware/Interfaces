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

package com.astrolab.Interfaces.spark

import org.scalatest.{BeforeAndAfterAll, FunSuite}

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

// Building loader for the two libraries
object LibraryLoader {
  lazy val load_my = {
    System.load(SparkFiles.get("libmy_udf.so"))
  }
}

/**

  * Test class

  */

class testJNASpark extends FunSuite with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
  }

  test("Launch a Spark pipeline that calls C functions via JNA"){

    val cores = 100
    val conf = new SparkConf().setMaster("local[*]").setAppName("TSpark").
      set("spark.cores.max", s"$cores").
      set("spark.executor.memory", "200g").
      set("spark.files", "libmy_udf.so")

    val nil: List[Double] = Nil

    val sc = new SparkContext(conf)
    val l = sc.parallelize((1 to 10)).
      map(x => {LibraryLoader.load_my; MyLibraries.my_lib.mysum(x, 12)}).
      map(x => {LibraryLoader.load_my; MyLibraries.my_lib.mymultiply(x.toDouble, 0.5)}).
      aggregate(nil)((x, y) => y :: x, (x, y) => y ::: x).
      toArray

    println(l.mkString(" "))
  }

}



