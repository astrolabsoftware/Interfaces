

// Structure used by pointer

typedef struct _Point {
  double x, y, z;
} Point;

Point* translate(Point* pt, double dx, double dy, double dz);

Point* translate(Point* pt, double dx, double dy, double dz) {
    pt->x += dx;
    pt->y += dy;
    pt->z += dz;

    return pt;
}

