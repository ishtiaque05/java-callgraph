#!/usr/bin/python3

import sys

# Dictionary of caller -> callee
# caller := <class>:<method>(<args>):<bci>
# calle := <class>:<method>(<args>):[<bci>]
callmap = {}

# Dictionary of callee -> caller
# caller := <class>:<method>(<args>):<bci>
# calle := <class>:<method>(<args>)
rcallmap = {}

static_max_frame = 40


# TODO - new solution: give each method call a hash (16 bits)
# TODO - maintain a 32 bit context register for each thread
#       - each time we go through a method call, add the hash.
#       - each time we return from a method call, subtract the hash.
#       - context (32 bits) = 16 bit acc + 16 bit method+bci
# TODO - static analyzer (python):
#       - removes methods calls that do not lead to allocations
#       - removes methods calls that are not decisive
#       - limit methods calls to a number of frames from an allocation site.
# TODO - statuc analyzer (java):
#       - gives each method call a unique hash (supports polymorphism) (16 bit)
#       - gives each allocatiohn site hash (16 bit)

# This method returns the inverse call graph that led to a callee.
# Target/calee is <class>:<method>(<args>)
def reverseTraversal(result, frames, callee, output, depth):
  if depth > static_max_frame:
    return

  if callee not in result:
    result[callee] = []

  callers = [] if callee not in rcallmap else rcallmap[callee]
  for caller in callers:
    if caller.startswith(sys.argv[2]):
      print("Found " + str(sys.argv[2]) + " at depth = " + str(depth))
#      printframes(frames, sys.stdout)
#      exit()

    if caller not in result[callee]:
      result[callee].append(caller)
    else:
      continue

    if caller not in frames[depth]:
      frames[depth].append(caller)
    else:
      continue

    striped = caller.split("#")[0]

# Debug
#    print("d=" + str(depth) + "\t", file=output, end="")
#    for i in range(0, depth): print(" ", end="", file=output)
#    print(caller, file=output)

    reverseTraversal(result, frames, striped, output, depth + 1)
  return

# Target is <class>:<method>(<args>). Reult is list of <class>:<method>(<args>)#<bci>
def findCallers(imap, target):
  result = []
  for caller in imap:
    if caller.startswith(target) and caller not in result:
      result.append(caller)
  return result

def findMultiCallers(imap, targets):
  result = []
  for target in targets:
    result.extend(findCallers(imap, target))
  return result

# Target is <class>:<method>#<bci>
# Imap is forward call map
def findDivergence(imap, result, stack, target, output):
  values = [] if target not in imap else imap[target]
  if values:
    callers = findMultiCallers(imap, values)
    if len(callers) > 1:
      result.extend(callers)
    for caller in callers:
      if caller in stack:
        continue
      else:
        stack.append(caller)
        findDivergence(imap, result, stack, caller, output)

def buildcallmap(caller, callee):
  if caller not in callmap:
    callmap[caller] = []

  if callee not in callmap[caller]:
    callmap[caller].append(callee)

# Find keys that are never values
# Note this is prepared to work with a forward call map
# in which keys have bci and values don't
def entrypoints(imap):
  result = []
  for k1 in imap:
    sk1 = k1.split("#")[0]
    found = False
    for k2 in imap:
      if sk1 in imap[k2]:
        found = True
        break
    if not found:
      result.append(k1)
  return list(set(result))

def swapmap(imap):
  result = {}
  for caller in imap:
    for callee in imap[caller]:
      if callee not in result:
        result[callee] = []
      if caller not in result[callee]:
        result[callee].append(caller)
  return result

def printframes(frames, output):
  i = 0
  for frame in frames:
    print ("Frame " + str(i))
    for elem in frame:
      print("\t" + elem)

def printmap(graph, output):
  for caller in graph:
    print(caller, file=output)
    for callee in graph[caller]:
      print("\t" + callee, file=output)

# Phase 1 - build callmap and reverse callmap
for line in sys.stdin:
  splits = line.strip().split(" ")
  caller = splits[0]
  callee = splits[2]
  buildcallmap(caller, callee)

rcallmap = swapmap(callmap)

print("callmap with " + str(len(callmap)) + " keys")
print("rcallmap with " + str(len(rcallmap)) + " keys")

print("Printing callmap")
output_file = open(sys.argv[1] + "/app.callmap", "w")
printmap(callmap, output_file)
output_file.close()

print("Printing rcallmap")
output_file = open(sys.argv[1] + "/app.rcallmap", "w")
printmap(rcallmap, output_file)
output_file.close()

# Phase 2 - find decisive method calls for each method where allocations occur.
result = []

with open(sys.argv[1] + "/app.news", "r") as f:
  for line in f:
    target=line.strip()

    print("Looking into " + target)
    revers = {}
    frames = [ [] for i in range(0, static_max_frame + 1)] 
    reverseTraversal(revers, frames, target, sys.stdout, 1)

# Debug
#    print("Printing reversed map")
#    printmap(revers, sys.stdout)
    for frame in frames:
      print("Frame with " + str(len(frame)) + " elements")
'''
    # TODO - need? Don't think so...
    allocmap = swapmap(revers)

# Debug   
#    print("Printing swaped reversed map")
#    printmap(allocmap, sys.stdout)

    # TODO - remove the idea of entry points, we use 25 frames now.
    print("Printing entry points for swapped reverse map")
    for entrypoint in entrypoints(allocmap):
      if not entrypoint.startswith(sys.argv[2]):
        continue
      print("\tentrypoint=" + entrypoint)
      # TODO - start from the bottom.
      findDivergence(allocmap, result, [], entrypoint, sys.stdout)

print("Printing important frames")
for i in set(result):
  print("important=" + i)
'''
