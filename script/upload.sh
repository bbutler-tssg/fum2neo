#!/bin/bash
# Copyright (c) 2017, Bernard Butler (Waterford Institute of Technology, Ireland), Project: SOLAS placement in Amadeus SA, where SOLAS (Project ID: 612480) is funded by the European Commision FP7 MC-IAPP-Industry-Academia Partnerships and Pathways scheme.
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
# 
#  -  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
#  -  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
#  -  Neither the name of WATERFORD INSTITUTE OF TECHNOLOGY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

function log() {
  now=`date +'%Y%m%d_%H:%M:%S'`
  echo $now $1
}

baseList=$1
addErrorsWarnings=${2:-n}

M2_REPO=$HOME/.m2/repository
avro=$M2_REPO/org/apache/avro/avro/1.8.2/avro-1.8.2.jar
jackson_core_asl=$M2_REPO/org/codehaus/jackson/jackson-core-asl/1.9.13/jackson-core-asl-1.9.13.jar
jackson_mapper_asl=$M2_REPO/org/codehaus/jackson/jackson-mapper-asl/1.9.13/jackson-mapper-asl-1.9.13.jar
paranamer=$M2_REPO/com/thoughtworks/paranamer/paranamer/2.7/paranamer-2.7.jar
snappy=$M2_REPO/org/xerial/snappy/snappy-java/1.1.1.3/snappy-java-1.1.1.3.jar
fum2neo=$HOME/fum2neo/target/classes
logback=$M2_REPO/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar:$M2_REPO/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar
jcommander=$M2_REPO/com/beust/jcommander/1.72/jcommander-1.72.jar
jackson=$M2_REPO/com/fasterxml/jackson/core/jackson-annotations/2.5.0/jackson-annotations-2.5.0.jar:$M2_REPO/com/fasterxml/jackson/core/jackson-core/2.5.4/jackson-core-2.5.4.jar:$M2_REPO/com/fasterxml/jackson/core/jackson-databind/2.5.4/jackson-databind-2.5.4.jar
caffeine=$M2_REPO/com/github/ben-manes/caffeine/caffeine/2.3.3/caffeine-2.3.3.jar
clhm=$M2_REPO/com/googlecode/concurrentlinkedhashmap/concurrentlinkedhashmap-lru/1.4.2/concurrentlinkedhashmap-lru-1.4.2.jar
commonslang=$M2_REPO/commons-lang/commons-lang/2.4/commons-lang-2.4.jar
opencsv=$M2_REPO/net/sf/opencsv/opencsv/2.3/opencsv-2.3.jar
commons=$M2_REPO/org/apache/commons/commons-compress/1.12/commons-compress-1.12.jar:$M2_REPO/org/apache/commons/commons-lang3/3.3.2/commons-lang3-3.3.2.jar
lucene=$M2_REPO/org/apache/lucene/lucene-analyzers-common/5.5.0/lucene-analyzers-common-5.5.0.jar:$M2_REPO/org/apache/lucene/lucene-backward-codecs/5.5.0/lucene-backward-codecs-5.5.0.jar:$M2_REPO/org/apache/lucene/lucene-codecs/5.5.0/lucene-codecs-5.5.0.jar:$M2_REPO/org/apache/lucene/lucene-core/5.5.0/lucene-core-5.5.0.jar:$M2_REPO/org/apache/lucene/lucene-queryparser/5.5.0/lucene-queryparser-5.5.0.jar
neo4jDriver=$M2_REPO/org/neo4j/driver/neo4j-java-driver/1.2.1/neo4j-java-driver-1.2.1.jar
neo4j=$M2_REPO/org/neo4j/neo4j/3.2.1/neo4j-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-codegen/3.2.1/neo4j-codegen-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-collections/3.2.1/neo4j-collections-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-command-line/3.2.1/neo4j-command-line-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-common/3.2.1/neo4j-common-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-configuration/3.2.1/neo4j-configuration-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-consistency-check/3.2.1/neo4j-consistency-check-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-csv/3.2.1/neo4j-csv-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-cypher/3.2.1/neo4j-cypher-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-cypher-compiler-3.2/3.2.1/neo4j-cypher-compiler-3.2-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-cypher-frontend-3.2/3.2.1/neo4j-cypher-frontend-3.2-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-cypher-ir-3.2/3.2.1/neo4j-cypher-ir-3.2-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-dbms/3.2.1/neo4j-dbms-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-graph-algo/3.2.1/neo4j-graph-algo-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-graphdb-api/3.2.1/neo4j-graphdb-api-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-graph-matching/3.1.3/neo4j-graph-matching-3.1.3.jar:$M2_REPO/org/neo4j/neo4j-import-tool/3.2.1/neo4j-import-tool-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-index/3.2.1/neo4j-index-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-io/3.2.1/neo4j-io-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-jmx/3.2.1/neo4j-jmx-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-kernel/3.2.1/neo4j-kernel-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-logging/3.2.1/neo4j-logging-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-lucene-index/3.2.1/neo4j-lucene-index-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-lucene-upgrade/3.2.1/neo4j-lucene-upgrade-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-primitive-collections/3.2.1/neo4j-primitive-collections-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-resource/3.2.1/neo4j-resource-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-udc/3.2.1/neo4j-udc-3.2.1.jar:$M2_REPO/org/neo4j/neo4j-unsafe/3.2.1/neo4j-unsafe-3.2.1.jar
asm=$M2_REPO/org/ow2/asm/asm/5.2/asm-5.2.jar
parboiled=$M2_REPO/org/parboiled/parboiled-core/1.1.7/parboiled-core-1.1.7.jar:$M2_REPO/org/parboiled/parboiled-scala_2.11/1.1.7/parboiled-scala_2.11-1.1.7.jar
scala=$M2_REPO/org/scala-lang/scala-library/2.11.8/scala-library-2.11.8.jar:$M2_REPO/org/scala-lang/scala-reflect/2.11.8/scala-reflect-2.11.8.jar
slf4j=$M2_REPO/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar
guava=$M2_REPO/com/google/guava/guava/23.0/guava-23.0.jar

classPath=$fum2neo:$logback:$jcommander:$jackson:$slf4j:$neo4jDriver:$guava:$avro:$jackson_core_asl:$jackson_mapper_asl
mainClass=org.tssg.solas.bb.fum2neo.UploadExtract

# See https://askubuntu.com/a/706683
writeAvro=y
updateNeo=n
declare -i i
i=1
for base in $baseList; do
  log "Read extract $base..."
  [ ! -d output/$base ] && mkdir -vp output/$base
  args="-i input/${base}.json -o output/$base/summary.csv"
  if [ "$addErrorsWarnings" == "y" ]; then
    args="${args} -e -w"
  fi
  if [ "$writeAvro" == "y" ]; then
    args="${args} -f WriteAvro -a output/${base}.gz.avro"
  fi
  if [ "$updateNeo" == "y" ]; then
    args="${args} -f UpdateNeo"
  fi
  java -Dfile.encoding=UTF-8 -classpath $classPath $mainClass $args && script/cleanupCsv.sh $base
  if [ $? -eq 0 ]; then
    log "Insert data extract $i in sqlite..."
    if [ $i -eq 1 ]; then
      script/createInsertDb.sh output/analysis.db output/$base/summary_table.csv
    else
      script/appendDb.sh output/analysis.db output/$base/summary_table.csv
    fi
    i+=1
  fi
done

log "analyse the data in sqlite"

script/analyse.sh output/analysis.db $base
