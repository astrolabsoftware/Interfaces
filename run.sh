SBT_VERSION=2.11.8
SBT_VERSION_SPARK=2.11

## Package version
VERSION=0.1.0

# Package it
sbt ++${SBT_VERSION} package

here=`pwd`
jna=~/.ivy2/cache/net.java.dev.jna/jna/jars/jna-4.5.1.jar

heap="-Xms1g"

export LD_LIBRARY_PATH=`pwd`
export JAVA_OPTS="-Xmx8G -Xms8G"

master="--master local[*]"
class="--class ca.HelloWorld"
jars="--jars=${jna}"
files="--files=${here}/libnative_udf.so"
target="target/scala-${SBT_VERSION_SPARK}/tjna_2.11-0.1.jar"

# Run it!
spark-submit ${master} ${class} ${jars} ${files} ${target}

# scalac


