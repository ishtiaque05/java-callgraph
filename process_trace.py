#!/usr/bin/python3

import sys

# Dictionary of caller -> callee
# caller := <class>:<method>(<args>):<bci>
# calle := <class>:<method>(<args>):[<bci>]
callmap = {}

# Dictionary of callee -> caller
# caller := <class>:<method>(<args>):<bci>
# calle := <class>:<method>(<args>):[<bci>]
rcallmap = {}

def callgraph(graph, output):
  for node in graph:
    print(node, file=output)
    callbranch(graph, node, 1, output)

def callbranch(graph, caller, depth, output):
  if caller not in graph:
    return
  for callee in graph[caller]:
    for i in range(0, depth):
      print("\t", end="", file=output)
    print(callee, file=output)
    callbranch(graph, callee, depth + 1, output)

def buildcallmap(caller, callee):
  if caller not in callmap:
    callmap[caller] = []

  if callee not in callmap[caller]:
    callmap[caller].append(callee)

def buildrcallmap():
  for caller in callmap:
    for callee in callmap[caller]:
      if callee not in rcallmap:
        rcallmap[callee] = []
      if caller not in rcallmap[callee]:
        rcallmap[callee].append(caller)

def finishcallmap():
  # This method will search and return a list of callees that are called by the
  # caller node. Note that this caller node does not contain a bci.
  def findcallers(callee):
    result = []
    for caller in callmap:
      if callee == caller.split("#")[0]:
        result.append(caller)
    return result

  # Up to this point, callees do not have a BCI. This cycle will add BCIs to
  # callees
  for caller in callmap:
    ncallees = []
    for callee in callmap[caller]:
      tmp = findcallers(callee)
      if not tmp:
        ncallees.append(callee)
      else:
        ncallees.extend(tmp)
    callmap[caller] = ncallees

def printgraph(graph, output):
  for caller in graph:
    print(caller, file=output)
    for callee in graph[caller]:
      print("\t" + callee, file=output)

for line in sys.stdin:
  splits = line.strip().split(" ")
  caller = splits[0]
  callee = splits[2]
  buildcallmap(caller, callee)

finishcallmap()
buildrcallmap()

print(sys.getrecursionlimit())
sys.setrecursionlimit(100000)

print("Printing callmap")
output_file = open(sys.argv[1] + "/app.callmap", "w")
printgraph(callmap, output_file)
output_file.close()

print("Going for call tree")
output_file = open(sys.argv[1] + "/app.callgraph", "w")
callgraph(callmap, output_file)
output_file.close()

print("Printing rcallmap")
output_file = open(sys.argv[1] + "/app.rcallmap", "w")
printgraph(rcallmap, output_file)
output_file.close()

print("Going for call tree")
output_file = open(sys.argv[1] + "/app.rcallgraph", "w")
callgraph(rcallmap, output_file)
output_file.close()
