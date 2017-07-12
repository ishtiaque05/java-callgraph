#!/bin/bash
target=$1
# TODO - remove [I] calls
java -classpath target/javacg-0.1-SNAPSHOT-static.jar gr.gousiosg.javacg.stat.JCallGraph  $target | grep "^M:" | grep -v "\[I\]" | sed 's/M://g' | sort | uniq  | ./process_trace.py
