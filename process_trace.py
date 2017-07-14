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

# Target is <class>:<method>(<args>)
def reverseTraversal(result, stack, callee, output, depth):
  callers = [] if callee not in rcallmap else rcallmap[callee]
  stripped_callers = []
  if callee not in result:
    result[callee] = []
  for caller in callers:
    if caller in stack:
      continue
    print("d=" + str(depth) + "\t", file=output, end="")
    for i in range(0, depth): print(" ", end="", file=output)
    print(caller, file=output)
    if caller not in result[callee]:
      result[callee].append(caller)
    stack.append(caller)
    striped = caller.split("#")[0]
    if striped not in stripped_callers:
      stripped_callers.append(striped)
      reverseTraversal(result, stack, striped, output, depth + 1)
    stack.pop()
  return result

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
def findDivergence(imap, stack, target, output):
  values = [] if target not in imap else imap[target]
  if values:
    callers = findMultiCallers(imap, values)
    if len(callers) > 1:
      print("important= " + str(callers))
    for caller in callers:
      if caller in stack:
        continue
      else:
        stack.append(caller)
        findDivergence(imap, stack, caller, output)
        stack.pop() 


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

def printmap(graph, output):
  for caller in graph:
    print(caller, file=output)
    for callee in graph[caller]:
      print("\t" + callee, file=output)

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

# Look in rcallgraph for any key
#target="gr.gousiosg.javacg.stat.MethodVisitor:prepareCall(org.apache.bcel.classfile.JavaClass,org.apache.bcel.generic.ReferenceType,org.apache.bcel.generic.InvokeInstruction)"
target="gr.gousiosg.javacg.stat.ClassVisitor:<init>(org.apache.bcel.classfile.JavaClass)"
print("Looking into " + target)
revers = reverseTraversal({}, [], target, sys.stdout, 1)
print("Printing reversed map")
printmap(revers, sys.stdout)
print("Printing swaped reversed map")
allocmap = swapmap(revers)
printmap(allocmap, sys.stdout)
print(entrypoints(allocmap))
for entrypoint in entrypoints(allocmap):
  findDivergence(allocmap, [], entrypoint, sys.stdout)
