

#include "native_udf.h"
#include <stdlib.h>
#include <stdio.h>

// Simple print function

void display(char* ch) {
    printf("display> %s", ch);
}

// Structure used by pointer

Point* translate(Point* pt, int dx, int dy) {
    pt->x += dx;
    pt->y += dy;

    return pt;
}




