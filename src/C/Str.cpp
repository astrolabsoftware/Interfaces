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

#include <iostream>
#include <string>
#include <string.h>
#include <stdlib.h>

using namespace std;

#include "MyUdf.h"

const string _myconcat (const string a, const string b);

const char* myconcat (const char* a, const char* b)
{
    static const char* from_cpp = _myconcat(string(a), string(b)).c_str();
    void* result = malloc(strlen(from_cpp) + 1);
    cout << "myconcat> " << " a=" << a << " b=" << b << " from_cpp=" << from_cpp << " pointer" << result << endl;
    strcpy((char*) result, from_cpp);
    return (char*) result;
}

void myfree(const void* str) {
    cout << "myfree> " << " pointer=" << str << endl;
    free((void*) str);
}

const string _myconcat (const string a, const string b)
{
    const string r = a + b;
    cout << "_myconcat> " << " a=" << a << " b=" << b << " r=" << r << endl;
    return r;
}

#ifdef MAIN

int main()
{
  const string result = _myconcat("aa", "bb");
  cout << "Result = " << result << endl;


  const char* result2 = myconcat("aaa", "bbb");
  cout << "Result = " << result2 << endl;
  myfree(result2);

  return 0;
}

#endif
