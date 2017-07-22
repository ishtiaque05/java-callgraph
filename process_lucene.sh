#!/bin/bash

basedir=$(dirname "$0")
source $basedir/process_include.sh
output_folder=$basedir


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

run
