#!/bin/bash

basedir=$(dirname "$0")
output_folder=$basedir
main="gr.gousiosg.javacg.stat.JCallGraph"

# Test purposes.
#target="target/classes/gr/gousiosg/javacg/stat/test/polymorphism"
#classpath="$classpath $basedir/target/javacg-0.1-SNAPSHOT-static.jar"
#entrypoint="gr.gousiosg.javacg.stat.test.polymorphism"


# Test 2
classpath="$classpath $basedir/target/javacg-0.1-SNAPSHOT-static.jar"
target=$classpath
entrypoint="gr.gousiosg.javacg.stat"
#entrypoint="gr.gousiosg.javacg.stat.test.polymorphism"


function run_python {
  echo "Launching python..."
  cat $output_folder/app.log | grep "^M:" | grep -v "\[I\]" | sed 's/M://g' | sort | uniq  | $basedir/process_trace.py $output_folder $entrypoint
  echo "Launching python...Done"
}

function run_java {
  echo "Launching java..."
  java -cp $classpath $main $target &> $output_folder/app.log 
  echo "Launching java...Done"

  cat app.log | grep "^N" | sed 's/NA://g' | sed 's/N://g' |  awk -F'#' '{print $1}' | sort | uniq &> $output_folder/app.news
  echo "Number of allocation methods = `cat $output_folder/app.news | wc -l`"
}

while true; do
  read -p "Run java tool? " run
  case $run in
    [Yy]* ) run_java; break;;
    [Nn]* ) break;;
        * ) echo "Please answer run or load. ";;
  esac
done
#run_java

while true; do
  read -p "Run python tool? " run
  case $run in
    [Yy]* ) run_python; break;;
    [Nn]* ) break;;
        * ) echo "Please answer run or load. ";;
  esac
done
#run_python

beep
