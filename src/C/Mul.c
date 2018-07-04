/*
 * Copyright 2018 Christian Arnault
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

extern "C" {
  double mymultiply(double x, double y);
  void myarray(double array[], int arraylen);
}

double mymultiply(double x, double y) {
  return (x * y);
}

void myarray(double array[], int arraylen) {
  int i = 0;
  for (i=0; i < arraylen; i++) { array[i] *= 2; }
}
