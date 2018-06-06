package ca

import com.sun.jna.{Library, Native, Platform}

import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext
import org.apache.spark.SparkFiles

// declaring to JNA
trait EntryPoints extends Library {
  def mysum(x: Int, y: Int): Int
  def mymultiply(x: Double, y: Double): Double
  def myarray(x: Array[Double], array_size: Int): Unit
  def cos(angle: Double): Double
}

object Libraries {
  def sum = Native.loadLibrary("sum", classOf[EntryPoints]).asInstanceOf[EntryPoints]
  def mul = Native.loadLibrary("mul", classOf[EntryPoints]).asInstanceOf[EntryPoints]
  def m = Native.loadLibrary("m", classOf[EntryPoints]).asInstanceOf[EntryPoints]
}

// Building loader for the two libraries
object LibraryLoader {
  lazy val loadsum = {
    System.load(SparkFiles.get("libsum.so"))
  }
  lazy val loadmul = {
    System.load(SparkFiles.get("libmul.so"))
  }

}

object HelloWorld {
  def time[R](text: String, block: => R, loops: Int = 1): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()

    var dt:Double = ((t1 - t0)/loops.toDouble).asInstanceOf[Double] / 1000000000.0

    val unit = "S"

    println("\n" + text + "> Elapsed time:" + " " + dt + " " + unit)

    result
  }

  def main(args: Array[String]) {
    println("HelloWorld")

    val r1 = Libraries.sum.mysum(1, 2)
    val r2 = Libraries.mul.mymultiply(1.111, 2.222)
    println("r1 = " + r1.toString + " r2 = " + r2.toString)

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
        Libraries.m.cos(angle)
      }
    }, 100000)

    val cores = 100
    val conf = new SparkConf().setMaster("local[*]").setAppName("TSpark").
      set("spark.cores.max", s"$cores").
      set("spark.executor.memory", "200g")

    val nil: List[Double] = Nil

    val sc = new SparkContext(conf)
    val l = sc.parallelize((1 to 10)).map(x => {
      LibraryLoader.loadsum; Libraries.sum.mysum(x, 12)
    }).
      map(x => {
        LibraryLoader.loadmul; Libraries.mul.mymultiply(x.toDouble, 0.5)
      }).
      aggregate(nil)((x, y) => y :: x, (x, y) => y ::: x).toArray
    println(l.mkString(" "))

    Libraries.mul.myarray(l, l.length)
    println(l.mkString(" "))
  }
}

