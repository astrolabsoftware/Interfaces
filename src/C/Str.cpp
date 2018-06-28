
#include <iostream>
#include <string>
#include <string.h>
#include <stdlib.h>

using namespace std;

extern "C" {
  const char* myconcat (const char* a, const char* b);
  void myfree(const void* str);
}

const string _myconcat (const string a, const string b);

const char* myconcat (const char* a, const char* b)
{
    static const char* r = _myconcat(string(a), string(b)).c_str();
    void* rr = malloc(strlen(r) + 1);
    cout << "myconcat> " << " a=" << a << " b=" << b << " r=" << r << " pointer" << rr << endl;
    strcpy((char*) rr, r);
    return (char*) rr;
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
