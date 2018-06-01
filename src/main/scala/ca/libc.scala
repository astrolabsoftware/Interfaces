  package ca

  import com.sun.jna.{Library, Native, Platform}

  trait MyLibrary extends Library {
    def mysum(x: Int, y: Int): Int
  }

  object MyLibrary {
    def lib = Native.loadLibrary("sum", classOf[MyLibrary]).asInstanceOf[MyLibrary]
  }


  object HelloWorld {
    def main(args: Array[String]) {
      val r = MyLibrary.lib.mysum(1, 2)
      println("r = " + r.toString)
    }
  }
