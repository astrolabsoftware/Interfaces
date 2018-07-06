
import matplotlib
print(matplotlib.get_backend())
matplotlib.use('Agg')


import matplotlib.pyplot as plt
import numpy as np

import time

def add(a, b):
    return a+b

def tarray():
    return np.arange(10)

def tint():
    return 12

z = 12.34

t = np.arange(0.0, 2.0, 0.01)
s = 1 + np.sin(2 * np.pi * t)

fig, ax = plt.subplots()
ax.plot(t, s)

fig.savefig("test")
# plt.show()


"""
fig, a = plt.subplots()
try:
  # plt.show(block=False)
  plt.show()
  plt.savefig('myfig')
  print("ne")
  # time.sleep(10)
except:
  print("e")
"""

