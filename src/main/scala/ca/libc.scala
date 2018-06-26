
package ca

import com.sun.jna.{Library, Native, Platform, Structure, Pointer}
import com.sun.jna.ptr.{IntByReference}

import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext
import org.apache.spark.SparkFiles


trait EntryPoints extends Library {
  def mysum(x: Int, y: Int): Int

  def mymultiply(x: Double, y: Double): Double
  def myarray(x: Array[Double], array_size: Int): Unit

  def translate(pt: ca.Point.P, x: Double, y: Double, z: Double): ca.Point.P
  def modify(ptr: IntByReference)

  def cos(angle: Double): Double
}

object Libraries {
  def native = Native.loadLibrary("native_udf", classOf[EntryPoints]).asInstanceOf[EntryPoints]
  def m = Native.loadLibrary("m", classOf[EntryPoints]).asInstanceOf[EntryPoints]
}

// Building loader for the two libraries
object LibraryLoader {
  lazy val loadnative = {
    System.load(SparkFiles.get("libnative_udf.so"))
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
      LibraryLoader.loadnative; Libraries.native.mysum(x, 12)
    }).
      map(x => {
        LibraryLoader.loadnative; Libraries.native.mymultiply(x.toDouble, 0.5)
      }).
      aggregate(nil)((x, y) => y :: x, (x, y) => y ::: x).toArray
    println(l.mkString(" "))

    println("===== Call a C function that modifies a Scala array")

    Libraries.native.myarray(l, l.length)
    println(l.mkString(" "))
  }

  def main(args: Array[String]) {

    println("===== Calling simple functions with numeric scalars")
    val r1 = Libraries.native.mysum(1, 2)
    val r2 = Libraries.native.mymultiply(1.111, 2.222)
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
        Libraries.m.cos(angle)
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
      Libraries.native.myarray(b, b.length)
      val after = b.sum

      //println (s"Apply function to an array: sum $before $after")
        result += after
      }
      result
    }, iterations)

    println (s"Apply function to an array calling a C function: $result")

    val pt = new ca.Point.P();
    val r3 = Libraries.native.translate(pt, 100.0, 100.0, 100.0);

    println(s"Translate a Point x=${pt.x} y=${pt.y} z=${pt.z}")

    val ptr = new IntByReference(10)
    Libraries.native.modify(ptr)
    println(s"ptr = ${ptr.getValue}")
  }
}

