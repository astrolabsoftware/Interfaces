# -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux"

gcc -fPIC -shared -o libsum.so Sum.c
