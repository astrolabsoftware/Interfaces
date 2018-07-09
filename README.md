
# Multi Language Interfaces <a name="Multi-Language-Interfaces"> </a>

This product proposes some way to interface different coding languages implied in the process
of scientific programming in the context of distributed programming such as Apache Spark,
and in particular, this concerns the development around the AstroLab Sofware organization, or
developments using it.

This describes the issues, problems, tools implied in interfacing various languages such as:

- C
- C++
- Fortran
- Java
- Scala
- Python

Generally speaking, since we are mainly concerned with the use of Apache Spark, which is mainly coded in Scala,
we mainly try to interface all scientific oriented languages to Scala.

<!-- toc -->
___

## Contents:

1. [Multi Language Interfaces](Multi-Language-Interfaces)
   1. [Using JNA](Using-JNA)
      1. [References for JNA](References-for-JNA)
      1. [How to link C/C++/Fortran -> Scala](How-to-link-C/C++/Fortran-->-Scala)
      1. [Exchanging structures](Exchanging-structures)
      1. [Using values by reference (ie: using pointers)](Using-values-by-reference--ie--using-pointers-)
      1. [How use external functions in a Spark pipeline](How-use-external-functions-in-a-Spark-pipeline)
      1. [Issues related with C++](Issues-related-with-C++)
      1. [Various tutos to explicit use cases](Various-tutos-to-explicit-use-cases)
   1. [Using the repository](Using-the-repository)
      1. [Compiling & building](Compiling-&-building)
   1. [Utilisation du package Jep pour interfacer Scala et Python.](Utilisation-du-package-Jep-pour-interfacer-Scala-et-Python.)
      1. [One example](One-example)
      1. [Result of the bench:](Result-of-the-bench-)
      1. [Example with matplotlib](Example-with-matplotlib)
      1. [With sbt](With-sbt)
   1. [Support](Support)
___

<!-- endtoc -->

## Using JNA<a name="#Using-JNA"></a>

JNA is a new generation of tools meant to interface JVM with native (compiled) code. It uses JNI to
dynamically (using the introspection features of Java) connect to compiled and linked entry points.

Basic data types (scalars and arrays) are directly usable, including ByReference access.

Complexed types (structures) need the construction of a Java equivalent Structure which requires an ordering operation
for the structure fields only available in Java, and based upon the introspection from Java.

### References for JNA <a name="References-for-JNA"> </a>

- https://github.com/java-native-access/jna
- https://maven.java.net/content/repositories/releases/net/java/dev/jna/jna/
- https://java-native-access.github.io/jna/4.2.0/overview-summary.html
- http://jnaexamples.blogspot.com/2012/03/java-native-access-is-easy-way-to.html
- https://www.sderosiaux.com/2016/08/03/jna-java-native-access-enjoy-the-native-functions


### How to link C/C++/Fortran -> Scala <a name="How-to-link-C/C++/Fortran-->-Scala"> </a>

To explain we start from a set of examples.

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

### Exchanging structures <a name="Exchanging-structures"> </a>

At a first approach we consider the exchange through pointers.

A C structure will be declared as a derived Java/Scala class of the jna's abstract "Structure" class.
This class only has to implement one abstract method: "getFieldOrder" that returns a List<String> giving
the ordered list of field names of the structure (to help JNA to perform introspection to the C objects).

Once this is done, referenced objects can be used in Java/Scala from/to C/C++.


### Using values by reference (ie: using pointers) <a name="Using-values-by-reference--ie--using-pointers-"> </a>

A value (in the Scala/Java world) can be viewed/transmitted by reference using the com.sun.jna.ptr.IntByReference
(and XxxByReference for other Scala/Java).

(cf. https://java-native-access.github.io/jna/4.2.1/com/sun/jna/ptr/ByReference.html)

```
======================  C ===============================
void modify(int* ptr);

void modify(int* ptr) {
*ptr = 12;
}
=========================================================

======================  Scala ===========================
import com.sun.jna.ptr.{IntByReference}

trait EntryPoints extends Library {
def modify(ptr: IntByReference)
}

...
val ptr = new IntByReference(10)
Libraries.native.modify(ptr)
println(s"ptr = ${ptr.getValue}")
...

==========================================================
```


### How use external functions in a Spark pipeline <a name="How-use-external-functions-in-a-Spark-pipeline"> </a>

The principle is to dynamically load the shared libraries right when it's needed, ie. within the lambda, executed
in the Spark operation (map/reduce/...) right when it's needed, ie. before calling the external functions.

In addition, all shared libraries has to be declared using the `--files <path/xxx.so>` option of the spark-submit
command line.

```

val l = sc.parallelize((1 to 10)).map(x => {LibraryLoader.loadsum; Libraries.sum.mysum(x, 12)})

```

It should be noted that the loader operation will ensure that the shared library(ies) will be serialized, then
transparently deployed to all workers

### Issues related with C++ <a name="Issues-related-with-C++"> </a>

The Jna's API is only able to understand C types. Then when dealing with C++ coding, a mangling is applied to
function names (to support mutiple function signatures !!). The declaration of native functions in the Scala/Java
world has to worry about the exact external naming scheme, thus the mangling has to be considered.

For both reasons it's asked to construct one C flavour of all C++ external functions. Or at least declare them
in a extern "C" block when possible (when the signature is compatible with a C syntax).

An other important aspect of the C/C++ interfacing to Scala/Java world, is the fact that generally non C/C++ scalar
types have to be manually allocated (on the heap) and when these objects have to be returned back to the Java world,
it will be required to take care of their deallocation.


The next example shows both the mangling aspect and the manual allocation of the returned value.

We consider une function that concatenates two std::string objects and return the result.

The primary C++ function that operates this operation can be written as follows (we first consider a version that
returns nothing):


```
#include <iostream>
#include <string>

using namespace std;

void _myconcat (const string a, const string b)
{
const string r = a + b;
cout << "_myconcat> " << " a=" << a << " b=" << b << " r=" << r << endl;
}


```

Of course this function will be mangled and contains C++ types not directly usable in Java. Thefore a C interface
flavour has to be introduced:

```
#include <iostream>
#include <string>

using namespace std;

extern "C" {
void myconcat (const char* a, const char* b);
}

void _myconcat (const string a, const string b):

void myconcat (const char* a, const char* b)
{
cout << "myconcat> " << " a=" << a << " b=" << b << endl;
_myconcat(string(a), string(b)).c_str();
}

void _myconcat (const string a, const string b)
{
const string r = a + b;
cout << "_myconcat> " << " a=" << a << " b=" << b << " r=" << r << endl;
}


```

If we look at the public entry points, we understand why the C++ entry point cannot declared as it is:


```

> nm libmy_udf.so | egrep " T "
...
00000000000010b2 T myconcat
...
000000000000122e T _Z9_myconcatNSt7__cxx1112basic_stringIcSt11char_traitsIcESaIcEEES4_
...

```

Then we change the function so as it returns the result of the concatenation:

```
#include <iostream>
#include <string>

using namespace std;

extern "C" {
const char* myconcat (const char* a, const char* b);
}

const string _myconcat (const string a, const string b);

const char* myconcat (const char* a, const char* b)
{
static const char* r = _myconcat(string(a), string(b)).c_str();
cout << "myconcat> " << " a=" << a << " b=" << b << " r=" << r << endl;
return r;
}

const string _myconcat (const string a, const string b)
{
const string r = a + b;
cout << "_myconcat> " << " a=" << a << " b=" << b << " r=" << r << endl;
return r;
}
```

if we run this implementation:

```
int main()
{
const string result = _myconcat("aa", "bb");
cout << "Result = " << result << endl;


const char* result2 = myconcat("aaa", "bbb");
cout << "Result = " << result2 << endl;

return 0;
}

> ./mytest
_myconcat>  a=aa b=bb r=aabb
Result = aabb
_myconcat>  a=aaa b=bbb r=aaabbb
myconcat>  a=aaa b=bbb r=a
Result = @@@

```

The result is lost because the local result computed from the C function is lost after the scope is closed
(this is not true for the C++ function since the string object is returned by value)

Thus we have to mimic the return by value mechanism by creating a string object (that eventually be
destroyed).

```
#include <iostream>
#include <string>
#include <string.h>
#include <stdlib.h>

using namespace std;

extern "C" {
const char* myconcat (const char* a, const char* b);
void myfree(const void* str);
}

const string _myconcat (const string a, const string b);

const char* myconcat (const char* a, const char* b)
{
static const char* r = _myconcat(string(a), string(b)).c_str();
void* rr = malloc(strlen(r) + 1);
cout << "myconcat> " << " a=" << a << " b=" << b << " r=" << r << " pointer" << rr << endl;
strcpy((char*) rr, r);
return (char*) rr;
}

void myfree(const void* str) {
cout << "myfree> " << " pointer=" << str << endl;
free((void*) str);
}

const string _myconcat (const string a, const string b)
{
const string r = a + b;
cout << "_myconcat> " << " a=" << a << " b=" << b << " r=" << r << endl;
return r;
}
```

Now the test program will behave properly:

```

> mytest
_myconcat>  a=aa b=bb r=aabb
Result = aabb
_myconcat>  a=aaa b=bbb r=aaabbb
myconcat>  a=aaa b=bbb r=aaabbb pointer0x1ad2c30
Result = aaabbb
myfree>  pointer=0x1ad2c30

```





### Various tutos to explicit use cases <a name="Various-tutos-to-explicit-use-cases"> </a>

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


## Using the repository <a name="Using-the-repository"> </a>

This development tries to apply the explanations written in this document. This is a SBT based structure,
ie. sources are located in the "src" directory, with the following structure:

```

src/C           C or C++ sources
src/main/java   Java sources
src/main/scala  Scala sources
src/main/python Python sources

src/test/scala  Unit test scala sources

```

Java & Scala sources are organized in the "com.astrolab.Interfaces...." packages.

At the top level, are the management tools:

- Makefile to build the C/C++ module as a shared library ("libmy_udf.so"). All C/C++ modules are gathered within one single shared library.
- build.sbt (together with project/* configuration files for SBT) to build and test the Scala elements.
- run.sh, shell script to run the Spark based application.

### Compiling & building <a name="Compiling-&-building"> </a>

Building the shared library grouping all C/C++ modules:

```
> make clean all test
```

This will create the ./libmy_udf.so shared library containing all C/C++ entry points callable from Java/Scala/Spark

Building the Scala applications:

```
> export LD_LIBRARY_PATH=`pwd`
> sbt package
> sbt test
```

Running the test program:

```
> export LD_LIBRARY_PATH=`pwd`
> ./run.sh
```

## Utilisation du package Jep pour interfacer Scala et Python. <a name="Utilisation-du-package-Jep-pour-interfacer-Scala-et-Python."> </a>


Références:

* https://pypi.python.org/pypi/jep
* https://github.com/ninia/jep

### One example <a name="One-example"> </a>

```
val jep = new Jep(new JepConfig().addSharedModules("numpy"))

jep.eval("import numpy as np")

val arraySize = 1000000

jep.set("x", 10)
jep.getValue("x")
jep.eval("y = np.random.rand(2, 3)")
jep.getValue("y.shape")
jep.eval("z = np.random.rand(arraySize)")
jep.getValue("z.shape")

{
val f = Array.fill(arraySize)(Random.nextFloat)
val nd = new NDArray[Array[Float]](f, arraySize)
jep.set("t", nd)
}
```

### Result of the bench: <a name="Result-of-the-bench-"> </a>

```
x=10>                        Elapsed time: 0.276785568 µs
getValue(x)>                 Elapsed time: 7.149102638 µs
y = np.random.rand(2, 3)>    Elapsed time: 20.37042373 µs
getValue(y.shape)>           Elapsed time: 11.65154456 µs
z = np.random.rand(1000000)> Elapsed time: 12.649986593 ms
getValue(z.shape)>           Elapsed time: 11.224750006 µs
xfer array                   Elapsed time: 14.170212113 ms
```

### Example with matplotlib <a name="Example-with-matplotlib"> </a>

```
import jep._

object Tester {

def plot: Unit = {
println("plot")
val jep = new Jep(new JepConfig().addSharedModules("numpy", "matplotlib"))

jep.eval("import numpy as np")
jep.eval("import matplotlib")
jep.eval("matplotlib.use('Agg')")
jep.eval("import matplotlib.pyplot as plt")

jep.eval("t = np.arange(0.0, 2.0, 0.01)")
jep.eval("s = 1 + np.sin(2 * np.pi * t)")

jep.eval("fig, ax = plt.subplots()")
jep.eval("ax.plot(t, s)")

jep.eval("fig.savefig('test')")
}

def main(args: Array[String]): Unit = {
plot
}
}
```

### With sbt <a name="With-sbt"> </a>


We suppose that we got jep from "pip install --user jep". Then build.sbt will look like:

```
name := "testjep"
version := "0.1"
scalaVersion := "2.11.8"

unmanagedBase := file("/home/christian.arnault/.local/lib/python3.5/site-packages/jep")


> sbt clean assembly "runMain ca.Tester"
```

## Support <a name="Support"> </a>

<p align="center"><img width="100" src="https://github.com/astrolabsoftware/spark-fits/raw/master/pic/lal_logo.jpg"/> <img width="100" src="https://github.com/astrolabsoftware/spark-fits/raw/master/pic/psud.png"/> <img width="100" src="https://github.com/astrolabsoftware/spark-fits/raw/master/pic/1012px-Centre_national_de_la_recherche_scientifique.svg.png"/></p>


