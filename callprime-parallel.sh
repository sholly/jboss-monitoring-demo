#!/bin/sh 
for i in {1..200} 
do
  curl -k https://jboss-monitoring-demo-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org/jboss-monitoring-demo/prime/9287377 & 
done
