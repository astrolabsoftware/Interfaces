SBT_VERSION=2.11.8
SBT_VERSION_SPARK=2.11

## Package version
VERSION=0.1.0

# Package it
sbt ++${SBT_VERSION} package

jna=~/.ivy2/cache/net.java.dev.jna/jna/jars/jna-4.5.1.jar

# Run it!
spark-submit \
  --master local[*] \
  --class ca.HelloWorld \
  --jars=${jna} \
  target/scala-${SBT_VERSION_SPARK}/tjna_2.11-0.1.jar 

