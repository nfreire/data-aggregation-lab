#!/bin/sh

export CLASSPATH=
for jar in `ls lib/*.jar`
do
  export CLASSPATH=$CLASSPATH:$jar
done
export CLASSPATH=$CLASSPATH

nohup java -Dsun.net.inetaddr.ttl=0 -Xmx16G -cp casestudy:classes:$CLASSPATH inescid.europeanarepository.HarvesterOaiPhmIntoDalRepository data $1 $2 $3 $4 >output-HarvesterOaiPhmIntoDalRepository.txt 2>&1 &
#java -Dsun.net.inetaddr.ttl=0 -Xmx16G -cp harvester:classes:$CLASSPATH inescid.europeanarepository.HarvesterOaiPhmIntoDalRepository data https://api.europeana.eu/oai/record $1 $2 $3 $4 

