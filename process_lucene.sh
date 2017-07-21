#!/bin/bash

basedir=$(dirname "$0")
output_folder=$basedir
main="gr.gousiosg.javacg.stat.JCallGraph"


home=/home/rbruno/git/lucene-6.1.0
jars="$home/build/analysis/common/lucene-analyzers-common-6.1.0-SNAPSHOT.jar"
#jars="$jars:$home/build/codecs/lucene-codecs-6.1.0-SNAPSHOT.jar"
jars="$jars:$home/build/core/lucene-core-6.1.0-SNAPSHOT.jar"
jars="$jars:$home/build/demo/lucene-demo-6.1.0-SNAPSHOT.jar"
#jars="$jars:$home/build/expressions/lucene-expressions-6.1.0-SNAPSHOT.jar"
jars="$jars:$home/build/facet/lucene-facet-6.1.0-SNAPSHOT.jar"
#jars="$jars:$home/build/grouping/lucene-grouping-6.1.0-SNAPSHOT.jar"
#jars="$jars:$home/build/highlighter/lucene-highlighter-6.1.0-SNAPSHOT.jar"
#jars="$jars:$home/build/join/lucene-join-6.1.0-SNAPSHOT.jar"
#jars="$jars:$home/build/memory/lucene-memory-6.1.0-SNAPSHOT.jar"
jars="$jars:$home/build/misc/lucene-misc-6.1.0-SNAPSHOT.jar"
jars="$jars:$home/build/queries/lucene-queries-6.1.0-SNAPSHOT.jar"
#jars="$jars:$home/build/queryparser/lucene-queryparser-6.1.0-SNAPSHOT.jar"
#jars="$jars:$home/build/sandbox/lucene-sandbox-6.1.0-SNAPSHOT.jar"
#jars="$jars:$home/build/spatial3d/lucene-spatial3d-6.1.0-SNAPSHOT.jar"
#jars="$jars:$home/build/spatial-extras/lucene-spatial-extras-6.1.0-SNAPSHOT.jar"

classpath="$jars:$basedir/target/javacg-0.1-SNAPSHOT-static.jar"
target=$jars
entrypoint="org.apache.lucene.demo.IndexFiles"

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
