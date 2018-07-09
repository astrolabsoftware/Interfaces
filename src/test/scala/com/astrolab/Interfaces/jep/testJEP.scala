package com.astrolab.Interface.jep

import jep._
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext

import scala.util.Random


object testJEP {

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

  def main(args: Array[String]): Unit = {
    println("TSpark")

    val cores = 100
    val conf = new SparkConf().setMaster("local[*]").setAppName("TSpark").
      set("spark.cores.max", s"$cores").
      set("spark.local.dir", "/mongo/log/tmp/").
      set("spark.executor.memory", "200g").
      set("spark.storageMemory.fraction", "0")

    val sc = new SparkContext(conf)
    val rdd = sc.parallelize((1 to 200)).map(x => (x, 1.0 + math.sin(2*math.Pi*x/100.0)))
    val p = rdd.collect.toArray
    println(p.mkString(" "))

    sc.stop

    plot(p)
  }
}
