#!/bin/bash

basedir=$(dirname "$0")
source $basedir/process_include.sh
output_folder=$basedir


home=/home/rbruno/git/openjdk-8-hotspot/test/gc/ng2c

classpath="$home:$basedir/target/javacg-0.1-SNAPSHOT-static.jar"
target=$home
entrypoint="Test2"

run
