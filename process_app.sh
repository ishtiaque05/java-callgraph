#!/bin/bash
java -jar target/javacg-0.1-SNAPSHOT-static.jar $1 | grep "^M:" | sed 's/M://g' | sort | uniq | ./process_trace.py
