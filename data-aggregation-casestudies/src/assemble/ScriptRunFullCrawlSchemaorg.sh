#!/bin/sh

export CLASSPATH=
for jar in `ls lib/*.jar`
do
  export CLASSPATH=$CLASSPATH:$jar
done
export CLASSPATH=$CLASSPATH

# nohup java -Djsse.enableSNIExtension=false -Dsun.net.inetaddr.ttl=0 -Xmx4G -cp casestudy:classes:$CLASSPATH inescid.dataaggregation.casestudies.schemaorgcrawling.ScriptRunFullCrawlForAnalisys data $1 $2 $3 $4 $5 $6 >output-ScriptRunFullCrawlSchemaorg.txt 2>&1 &
nohup java -Djsse.enableSNIExtension=false -Dsun.net.inetaddr.ttl=0 -Xmx4G -cp casestudy:classes:$CLASSPATH inescid.dataaggregation.casestudies.schemaorgcrawling.ScriptRunFullCrawlForAnalisys data/schemaorgcrawling data/HttpRepository 1000 false >output-ScriptRunFullCrawlSchemaorg.txt 2>&1 &
