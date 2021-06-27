#!/bin/sh
for i in {1..10}; 
do
    curl -k https://jboss-monitoring-demo-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org/jboss-monitoring-demo/parallel&
done
sleep 2
curl -H "Accept: application/json" jboss-service-metrics-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org/metrics/application
curl -k https://jboss-monitoring-demo-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org/jboss-monitoring-demo/parallel-finish
