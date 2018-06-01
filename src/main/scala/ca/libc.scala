package ca

import com.sun.jna.{Library, Native, Platform}

import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext
import org.apache.spark.SparkFiles


trait MyLibrary extends Library {
  def mysum(x: Int, y: Int): Int
}

object MyLibrary {
  def lib = Native.loadLibrary("sum", classOf[MyLibrary]).asInstanceOf[MyLibrary]
}

object LibraryLoader {
  lazy val load = System.load(SparkFiles.get("libsum.so"))
}

object HelloWorld {
  def main(args: Array[String]) {
    println("HelloWorld")

    val r = MyLibrary.lib.mysum(1, 2)
    println("r = " + r.toString)

    val cores = 100
    val conf = new SparkConf().setMaster("local[*]").setAppName("TSpark").
        set("spark.cores.max", s"$cores").
        set("spark.executor.memory", "200g")

    val sc = new SparkContext(conf)
    val rdd = sc.parallelize((1 to 10)).map(x => {LibraryLoader.load; MyLibrary.lib.mysum(x, 12) } )
    println(rdd.collect.mkString(" "))
  }
}

