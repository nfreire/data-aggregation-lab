#!/bin/sh

export CLASSPATH=
for jar in `ls lib/*.jar`
do
  export CLASSPATH=$CLASSPATH:$jar
done
export CLASSPATH=$CLASSPATH

nohup java -Djsse.enableSNIExtension=false -Dsun.net.inetaddr.ttl=0 -Xmx4G -cp test_completeness_calc:classes:$CLASSPATH inescid.dataaggregation.dataset.profile.completeness.ScriptTestCalculator europeana-dataset-uris completeness-charts rdf-cache 500 -1 $2 $3 $4  >output-ScriptCompletenessCalculation.txt 2>&1 &
