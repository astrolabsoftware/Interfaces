# tjna

test code to experiment JNA + Scala + Spark

How to link C/C++/Fortran -> Scala
==================================

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

Exchanging structures
=====================

At a first approach we consider the exchange through pointers.

A C structure will be declared as a derived Java/Scala class of the jna's abstract "Structure" class.
This class only has to implement one abstract method: "getFieldOrder" that returns a List<String> giving
the ordered list of field names of the structure (to help JNA to perform introspection to the C objects).

Once this is done, referenced objects can be used in Java/Scala from/to C/C++.



How use external functions in a Spark pipeline
==============================================

The principle is to dynamically load the shared libraries right when it's needed, ie. inside the lambda executed
in the Spark commands (map/reduce/...) immediately before calling the external functions.

In addition, all shared libraries has to be declared using the `--files <path/xxx.so>` option of the spark-submit
main command line.

```

    val l = sc.parallelize((1 to 10)).map(x => {LibraryLoader.loadsum; Libraries.sum.mysum(x, 12)})

```

It should be noted that the loader operation will ensure that the shared library(ies) will be serialized, then
transparently deployed to all workers

Various tutos to explicit use cases
===================================

References:

- https://github.com/java-native-access/jna
- https://maven.java.net/content/repositories/releases/net/java/dev/jna/jna/
- https://java-native-access.github.io/jna/4.2.0/overview-summary.html 
- http://jnaexamples.blogspot.com/2012/03/java-native-access-is-easy-way-to.html


This tuto directory includes:

- One C module: src/native_udf.c + src/native_udf.h built into the linux directory
- One Java file to declare the entry points: ca/CUDF.java
- One Java application : ca/App.java
- One Makefile implementing some targets:
    + make clean
    + make lib
    + make classes
    + make all = lib + classes
    + make run triggers all

