

JNA=/home/arnault/.ivy2/cache/net.java.dev.jna/jna/jars/jna-4.5.1.jar

LIBDIR = .
SRCDIR = src/C
JAVASRC = ca

NATIVE = native_udf
LIB = lib$(NATIVE).so
LIBSOURCES = $(SRCDIR)/Mul.c $(SRCDIR)/Point.c $(SRCDIR)/Sum.c

all: lib
	echo "all done"

lib : $(LIBDIR)/$(LIB)
	echo "lib done"

$(LIBDIR)/$(LIB) : $(LIBSOURCES)
	gcc -shared -fPIC $(LIBSOURCES) -o $(LIBDIR)/$(LIB)

clean:
	rm -f $(LIBDIR)/$(LIB)

