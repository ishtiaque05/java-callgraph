#!/bin/bash

# Example: ./process_collisions.sh results/graphchi/
# TODO - look into logs and remove alloc sites that did not get expanded.

basedir=$(dirname "$0")

classpath="$basedir/target/javacg-0.1-SNAPSHOT-static.jar"
target=$classpath
main="gr.gousiosg.javacg.stat.CollisionFinder"

# Generate mid file
cat $1/app.log | grep "MID:" > $1/mids.log
# Generate nid file
cat $1/app.log | grep "NID:" > $1/nids.log
# Generate callmap file
cat $1/app.log | grep "M:" > $1/calls.log

echo "Launching java..."
time java -cp $classpath $main $1/mids.log $1/nids.log $1/calls.log &> $1/collisions.log
echo "Launching java...Done"
