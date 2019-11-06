#!/bin/sh

export CLASSPATH=
for jar in `ls lib/*.jar`
do
  export CLASSPATH=$CLASSPATH:$jar
done
export CLASSPATH=$CLASSPATH

java -Djsse.enableSNIExtension=false -Dsun.net.inetaddr.ttl=0 -Xmx4G -cp cs_evaluate:classes:$CLASSPATH inescid.dataaggregation.tests.ScriptEvaluateQualityOfIngestedDatasets  >output-ScriptEvaluateQualityOfIngestedDatasets.txt 2>&1 &
