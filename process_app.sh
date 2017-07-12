#!/bin/bash

basedir=$(dirname "$0")
output_folder=$basedir
main="gr.gousiosg.javacg.stat.JCallGraph"

# Test purposes.
#target="target/classes/gr/gousiosg/javacg/stat/test/polymorphism"
#classpath="$classpath $basedir/target/javacg-0.1-SNAPSHOT-static.jar"

# Test 2
classpath="$classpath $basedir/target/javacg-0.1-SNAPSHOT-static.jar"
target=$classpath

echo "Launching java..."
java -cp $classpath $main $target &> $output_folder/app.log 
echo "Launching java...Done"

echo "Launching python..."
#cat $output_folder/app.log | grep "^M:" | grep -v "\[I\]" | sed 's/M://g' | sort | uniq  | $basedir/process_trace.py $output_folder
echo "Launching python...Done"
