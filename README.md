# tjna

test code to experiment JNA + Scala + Spark

How to link C/C++/Fortran -> Scala
==============================

- we consider a library with offering entry points (C/C++/Fortran)

  - so far the following types haves been tested:

```

      C         Scala
     -------------------------
      int       Int
      double    Double
      double[]  Array[Double]

```

- How to declare those entry points using JNA:

  - import the JNA stuff:

```

import com.sun.jna.{Library, Native, Platform}

```

  - create a Scala trait to declare the signature of all external entry points, using a Scala syntax

```

// declaring to JNA
trait EntryPoints extends Library {
  def mysum(x: Int, y: Int): Int
  def mymultiply(x: Double, y: Double): Double
  def myarray(x: Array[Double], arrayln: Int): Unit
}

```

  - install, within a Scala object, the loading actions to load the shared libraries implementing the external functions

```

object Libraries {
  def sum = Native.loadLibrary("sum", classOf[EntryPoints]).asInstanceOf[EntryPoints]
  def mul = Native.loadLibrary("mul", classOf[EntryPoints]).asInstanceOf[EntryPoints]
}


```

- Calling the external functions:

```

    val r1 = Libraries.sum.mysum(1, 2)
    val r2 = Libraries.mul.mymultiply(1.111, 2.222)


```

- Exchanging arrays

We may pass an array to the external code. The array can be modified in place.

suppose yu create a C function that changes an array as follows:

```

void myarray(double array[], int arraylen) {
  int i = 0;
  for (i=0; i < arraylen; i++) { array[i] *= 2; }
}


```


```

    val a = Array[Double](...)
    Libraries.mul.myarray(a, a.length)


```

- Overhead in calling a C function:

Of course, using JNA has a cost. Comparing calling the math "cos" function from straight Scala and using the C cos through JNA:

  - scala cos> 0.34 µs

  - C cos> 5.3 µs


How use external functions in a Spark pipeline
==============================================

The principle is to dynamically load the shared libraries right when it's needed, ie. inside the lambda executed
in the Spark commands (map/reduce/...) immediately before calling the external functions.

In addition, all shared libraries has to be declared using the `--files <path/xxx.so>` option of the spark-submit
main command line.

```

    val l = sc.parallelize((1 to 10)).map(x => {LibraryLoader.loadsum; Libraries.sum.mysum(x, 12)})

```

It shou be noted that the loader operation will ensure that the shared library(ies) will be serialized, then
transparently deployed to all workers


