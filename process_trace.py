#!/usr/bin/python3

import sys

# Dictionary of caller -> callee
callgraph = {}

for line in sys.stdin:
  splits = line.strip().split(" ")
  caller = splits[0]
  callee = splits[1]

  if not caller in callgraph:
    callgraph[caller] = [callee]
  else:
    callgraph[caller] = callgraph[caller].append(callee)

for caller, calleelst in callgraph.items():
  print(caller)
  for callee in calleelst:
    print("\t" + callee)
