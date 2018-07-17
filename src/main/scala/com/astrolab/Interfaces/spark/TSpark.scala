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

import jep._
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext

import scala.util.Random


object TSpark {

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

    val cores = 4
    val conf = new SparkConf().setMaster("local[*]").setAppName("TSpark").
        set("spark.cores.max", s"$cores").
        set("spark.executor.memory", "200g")

    val sc = new SparkContext(conf)
    val rdd = sc.parallelize((1 to 200)).map(x => (x, 1.0 + math.sin(2*math.Pi*x/100.0)))
    val p = rdd.collect.toArray
    println(p.mkString(" "))

    sc.stop

    plot(p)

  }
}
