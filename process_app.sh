#!/bin/bash
target=$1
java -classpath target/javacg-0.1-SNAPSHOT-static.jar gr.gousiosg.javacg.stat.JCallGraph  $target #| grep "^M:" | sed 's/M://g' | sort | uniq | ./process_trace.py
