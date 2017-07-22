#!/bin/bash

function run_python {
  echo "Launching python..."
  cat $output_folder/app.log | grep "^M:" | sed 's/^M://g' | sort | uniq  | $basedir/process_trace.py $output_folder $entrypoint
  echo "Launching python...Done"
}

function run_java {
  main="gr.gousiosg.javacg.stat.JCallGraph"
  echo "Launching java..."
  java -cp $classpath $main $target &> $output_folder/app.log 
  echo "Launching java...Done"

  cat app.log | grep "^N:" | sed 's/^N://g' |  awk -F'#' '{print $1}' | sort | uniq &> $output_folder/app.news
  echo "Number of allocation methods = `cat $output_folder/app.news | wc -l`"
  echo "Number of allocation sites = `cat $output_folder/app.log | grep \"^NID:\" | wc -l`"
  echo "Number of caller methods = `cat $output_folder/app.log | grep \"^MID:\" | wc -l`"
}

function run {
  while true; do
    read -p "Run java tool? " run
    case $run in
      [Yy]* ) run_java; break;;
      [Nn]* ) break;;
          * ) echo "Please answer run or load. ";;
    esac
  done

  while true; do
    read -p "Run python tool? " run
    case $run in
      [Yy]* ) run_python; break;;
      [Nn]* ) break;;
          * ) echo "Please answer run or load. ";;
    esac
  done

  beep
}
