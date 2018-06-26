
package ca

import com.sun.jna.{Library, Native, Platform, Structure}

import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext
import org.apache.spark.SparkFiles
import org.apache.spark.HashPartitioner

object Parts {

  def internalGroupBy(iter: Iterator[(Int, Int)]): Iterator[(Int, Array[Int])] = {
    iter.toArray.groupBy(_._1).mapValues(_.map(_._2)).iterator
  }

  def run_spark = {

    val cores = 100
    val conf = new SparkConf().setMaster("local[*]").setAppName("TSpark").
      set("spark.cores.max", s"$cores").
      set("spark.executor.memory", "200g")


    val sc = new SparkContext(conf)
    val a0 = sc.parallelize(1 to 1000)

    println("------------a2")

    var a = a0.map(x => (x, x))

    for (p <- 1 to 6)
      {
        val fact = scala.math.pow(2, p).toInt
        a = a
          // .map(x => (x, x))
          .partitionBy(new HashPartitioner(1))
          .mapPartitions(internalGroupBy)
          .partitionBy(new HashPartitioner(fact))
          // .mapPartitions(_.map(_._2))
          .flatMap(x => x)
      }
    a = a.mapPartitions(_.map(_._2)).flatMap(x => x)
    val r = a.glom.collect
    for(x <- r) { for (y <- x) print(y + ","); println}
  }

  def main(args: Array[String]) {
    println("Parts")
    run_spark
  }
}

