#---------------------------------------------------
# Copyright 2018 Christian Arnault
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#---------------------------------------------------


SBT_VERSION=2.11.8
SBT_VERSION_SPARK=2.11

## Package version
VERSION=0.1.0

# Package it
# sbt ++${SBT_VERSION} package


here=`pwd`
jna=${HOME}/.ivy2/cache/net.java.dev.jna/jna/jars/jna-4.5.1.jar
jepdir="/usr/local/lib/python3.5/dist-packages/jep/"
jep="${jepdir}/jep-3.7.1.jar"

export LD_LIBRARY_PATH=${here}:${jepdir}
export LD_PRELOAD="/usr/lib/x86_64-linux-gnu/libpython3.5m.so"

master="--master local[*]"
class1="--class com.astrolab.Interfaces.spark.TSpark"
class2="--class com.astrolab.Interfaces.suite.TestSuite"
files="--files=${here}/libmy_udf.so"
jars="--jars ${jna},${jep}"
target="target/scala-2.11/interfaces_2.11-0.1.0.jar"

echo "======================================== TSpark ================================="
class=${class1}

command="spark-submit ${master} ${files} ${jars} ${class} ${target}"

# Run it!
echo "${command} $*"
${command} $*

echo "======================================== TestSuite ================================="

class=${class2}

command="spark-submit ${master} ${files} ${jars} ${class} ${target}"

# Run it!
echo "${command} $*"
${command} $*

