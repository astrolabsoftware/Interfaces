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

COMPILER = g++
CPPFLAGS = -Wall -g -shared -fPIC -lstdc++

JNA=${HOME}/.ivy2/cache/net.java.dev.jna/jna/jars/jna-4.5.1.jar

LIBDIR = .
SRCDIR = src/C

NATIVE = my_udf
LIB = lib$(NATIVE).so
LIBSOURCES = $(SRCDIR)/Mul.c $(SRCDIR)/Array.c $(SRCDIR)/Point.c $(SRCDIR)/Sum.c $(SRCDIR)/Str.cpp

all: lib
	echo "all done"

lib : $(LIBDIR)/$(LIB)
	echo "lib done"

$(LIBDIR)/$(LIB) : $(LIBSOURCES)
	$(COMPILER) $(CPPFLAGS) $(LIBSOURCES) -o $(LIBDIR)/$(LIB)

clean:
	rm -f $(LIBDIR)/$(LIB)

test: hello
	./hello

hello : $(LIBSOURCES)
	$(COMPILER) -DMAIN -Wall -g -fPIC -lstdc++ $(LIBSOURCES) -o hello
