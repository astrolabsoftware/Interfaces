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
sbt ++${SBT_VERSION} package

here=`pwd`
jna=~/.ivy2/cache/net.java.dev.jna/jna/jars/jna-4.5.1.jar

heap="-Xms1g"

export LD_LIBRARY_PATH=`pwd`
export JAVA_OPTS="-Xmx8G -Xms8G"

master="--master local[*]"
class="--class com.astrolab.Interfaces.HelloWorld"
jars="--jars=${jna}"
files="--files=${here}/libmy_udf.so"
target="target/scala-${SBT_VERSION_SPARK}/interfaces_${SBT_VERSION_SPARK}-${VERSION}.jar"

command="spark-submit ${master} ${class} ${jars} ${target}"

## ${files}

# Run it!
echo "${command}"
${command}



