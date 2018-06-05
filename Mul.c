
double mymultiply(double x, double y) {
  return (x * y);
}

double* myarray(double array[], int arraylen) {
  for (int i=0; i < arraylen; i++) { array[i] *= 2; }
  return (array);
}
