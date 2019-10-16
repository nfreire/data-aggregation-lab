#!/bin/sh

export CLASSPATH=
for jar in `ls lib/*.jar`
do
  export CLASSPATH=$CLASSPATH:$jar
done
export CLASSPATH=$CLASSPATH

nohup java -Djsse.enableSNIExtension=false -Dsun.net.inetaddr.ttl=0 -Xmx16G -cp casestudy:classes:$CLASSPATH inescid.dataaggregation.casestudies.ontologies.reasoning.ScriptBuildWikidataMetamodelJoin data $1 $2 $3 $4 >output-ScriptBuildWikidataMetamodelJoin.txt 2>&1 &
