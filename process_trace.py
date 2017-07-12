#!/usr/bin/python3

import sys

# Dictionary of caller -> callee
# caller := <class>:<method>(<args>):<bci>
# calle := <class>:<method>(<args>):[<bci>]
callgraph = {}

# This method will search and return a list of callees that are called by the
# caller node. Note that this caller node does not contain a bci.
def findcallers(callee):
  result = []
  for caller in callgraph:
    if callee == caller.split("#")[0]:
      result.append(caller)
  return result

def calltree (caller, depth):
  if caller not in callgraph:
    return
  for callee in callgraph[caller]:
    for i in range(0, depth):
      print("\t", end="")
    print(callee)
    calltree(callee, depth + 1)

def buildcallgraph(caller, callee):
  if caller not in callgraph:
    callgraph[caller] = []

  if callee not in callgraph[caller]:
    callgraph[caller].append(callee)

def finishcallgraph():
  # Up to this point, callees do not have a BCI. This cycle will add BCIs to
  # callees
  for caller in callgraph:
    ncallees = []
    for callee in callgraph[caller]:
      tmp = findcallers(callee)
      if not tmp:
        ncallees.append(callee)
      else:
        ncallees.extend(tmp)
    callgraph[caller] = ncallees

def printgraph(graph):
  # Printing callgraph
  for caller in graph:
    print(caller)
    for callee in graph[caller]:
      print("\t" + callee)

for line in sys.stdin:
  splits = line.strip().split(" ")
  caller = splits[0]
  callee = splits[2]
  buildcallgraph(caller, callee)

finishcallgraph()
printgraph(callgraph)

print("Going for call tree")
#string="gr.gousiosg.javacg.stat.test.polymorphism.Test:main(java.lang.String[])#29"
#print(string)
#calltree(string, 1)

for caller in callgraph:
  print(caller)
  calltree(caller, 1)

