#!/bin/bash

if [ -z "$1" ]
then
  echo "Использование get-process.sh <путь-к-Zeebe-partition>"
  exit 0
fi

if [ -z "$JAVA_HOME" ]
then
  JAVA_EXECUTION="java"
else
  JAVA_EXECUTION="$JAVA_HOME/bin/java"
fi

PARTITION_PATH=$1

function exportProcess {
  while read -r process; do
    echo "извлекаю процесс $process"
    PROCESS_DATA=$($JAVA_HOME/bin/java -jar ./zdb/cli-1.9.0-SNAPSHOT-jar-with-dependencies.jar process entity $process -p=$PARTITION_PATH | jq . )
    echo $PROCESS_DATA > ./processes/$process.json
  done
}

mkdir -p processes
$JAVA_HOME/bin/java -jar ./zdb/cli-1.9.0-SNAPSHOT-jar-with-dependencies.jar process list -p=$PARTITION_PATH | jq .[].processDefinitionKey | exportProcess