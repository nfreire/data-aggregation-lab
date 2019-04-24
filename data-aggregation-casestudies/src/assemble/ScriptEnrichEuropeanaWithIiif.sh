#!/bin/sh

export CLASSPATH=
for jar in `ls lib/*.jar`
do
  export CLASSPATH=$CLASSPATH:$jar
done
export CLASSPATH=$CLASSPATH

nohup java -Djsse.enableSNIExtension=false -Dsun.net.inetaddr.ttl=0 -Xmx4G -cp casestudy:classes:$CLASSPATH inescid.dataaggregation..casestudies.wikidata.iiif.EnrichEuropeanaWithIiif output http-cache $2 $3 $4  >output-ScriptEnrichEuropeanaWithIiif.txt 2>&1 &
