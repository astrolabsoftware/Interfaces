package ca

import com.sun.jna.{Library, Native, Platform}

import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext
import org.apache.spark.SparkFiles

// declaring to JNA
trait MyLibrary extends Library {
  def mysum(x: Int, y: Int): Int
  def mymultiply(x: Double, y: Double): Double
}

object MyLibrary {
  def lib1 = Native.loadLibrary("sum", classOf[MyLibrary]).asInstanceOf[MyLibrary]
  def lib2 = Native.loadLibrary("mul", classOf[MyLibrary]).asInstanceOf[MyLibrary]
}

// Building loader for the two libraries
object LibraryLoader {
  lazy val load1 = {
    System.load(SparkFiles.get("libsum.so"))
  }
  lazy val load2 = {
    System.load(SparkFiles.get("libmul.so"))
  }

}

object HelloWorld {
  def main(args: Array[String]) {
    println("HelloWorld")

    val r1 = MyLibrary.lib1.mysum(1, 2)
    val r2 = MyLibrary.lib2.mymultiply(1.111, 2.222)
    println("r1 = " + r1.toString + " r2 = " + r2.toString)

    val cores = 100
    val conf = new SparkConf().setMaster("local[*]").setAppName("TSpark").
        set("spark.cores.max", s"$cores").
        set("spark.executor.memory", "200g")

    val sc = new SparkContext(conf)
    val rdd = sc.parallelize((1 to 10)).map(x => {LibraryLoader.load1; MyLibrary.lib1.mysum(x, 12) } ).map(x => {LibraryLoader.load2; MyLibrary.lib2.mymultiply(x.toDouble, 0.5)})
    println(rdd.collect.mkString(" "))
  }
}

