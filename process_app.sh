#!/bin/bash

basedir=$(dirname "$0")
source $basedir/process_include.sh
output_folder=$basedir

# Test purposes.
#target="target/classes/gr/gousiosg/javacg/stat/test/polymorphism"
#classpath="$basedir/target/javacg-0.1-SNAPSHOT-static.jar"
#entrypoint="gr.gousiosg.javacg.stat.test.polymorphism"


# Test 2
classpath="$basedir/target/javacg-0.1-SNAPSHOT-static.jar"
target=$classpath
entrypoint="gr.gousiosg.javacg.stat"
#entrypoint="gr.gousiosg.javacg.stat.test.polymorphism"


run
