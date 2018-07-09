
package com.astrolab.Interface.jep

import org.scalatest.{BeforeAndAfterAll, FunSuite}

import jep._


import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext

import scala.util.Random


/*
/usr/local/lib/python3.5/dist-packages/jep/jep-3.7.1.jar
/usr/local/lib/python3.5/dist-packages/jep/jep.cpython-35m-x86_64-linux-gnu.so
/usr/local/lib/python3.5/dist-packages/jep/libjep.so
/usr/local/lib/python3.5/dist-packages/jep-3.7.1.egg-info
 */


class testJEP extends FunSuite with BeforeAndAfterAll {

  def plot(data: Array[(Int, Double)]): Unit = {
    println("plot")

    val jep = new Jep(new JepConfig().addSharedModules("numpy", "matplotlib"))

    jep.eval("import numpy as np")
    jep.eval("import matplotlib")
    jep.eval("matplotlib.use('Agg')")
    jep.eval("import matplotlib.pyplot as plt")

    def nd_set(x: Array[Float]) = new NDArray[Array[Float]](x, x.length)

    val indices = nd_set(data.map(x => x._1.toFloat))
    val values = nd_set(data.map(x => x._2.toFloat))
    jep.set("t", indices)
    jep.set("s", values)

    // jep.eval("t = np.arange(0.0, 2.0, 0.01)")
    // jep.eval("s = 1 + np.sin(2 * np.pi * t)")

    jep.eval("fig, ax = plt.subplots()")
    jep.eval("ax.plot(t, s)")

    jep.eval("fig.savefig('test')")
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
  }

  test("Launch a Spark pipeline") {
    val cores = 50
    val conf = new SparkConf().setMaster("local[*]").setAppName("TSpark").
      set("spark.cores.max", s"$cores").
      set("spark.executor.memory", "200g").
      set("spark.storageMemory.fraction", "0").
      set("spark.driver.allowMultipleContexts", "true")

    val sc = new SparkContext(conf)
    val rdd = sc.parallelize((1 to 200)).map(x => (x, 1.0 + math.sin(2*math.Pi*x/100.0)))
    val p = rdd.collect.toArray
    println(p.mkString(" "))

    sc.stop

    plot(p)
  }
}
