

JNA=/home/arnault/.ivy2/cache/net.java.dev.jna/jna/jars/jna-4.5.1.jar

LIBDIR = linux
SRCDIR = src
JAVASRC = ca

NATIVE = native_udf
LIB = lib$(NATIVE).so
LIBSOURCES = $(SRCDIR)/$(NATIVE).c
CLASSES = $(JAVASRC)/App.class $(JAVASRC)/CUDF.class

all: lib classes
	echo "all done"

lib : $(LIBDIR)/$(LIB)
	echo "lib done"

$(LIBDIR)/$(LIB) : $(LIBSOURCES)
	gcc -shared -fPIC $(LIBSOURCES) -o $(LIBDIR)/$(LIB)

clean:
	rm -f $(LIBDIR)/* $(JAVASRC)/*.class

classes : $(CLASSES)
	echo "classes done"

$(CLASSES): $(JAVASRC)/App.java $(JAVASRC)/CUDF.java
	javac -cp $(JNA) $(JAVASRC)/*.java

run: all
	java -cp $(CURDIR)/${LIBDIR}:${JNA}:$(CURDIR) ca.App
