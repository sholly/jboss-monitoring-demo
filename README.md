# Openshift Monitoring and Logging Demo

## Code

This is an extremely simple example based on https://github.com/wildfly/quickstart/tree/master/microprofile-metrics. 

Open src/main/java/com/redhat/demo/PrimeChecker.java and examine the metrics: 

1. @Counted: This annotation exposes a counter which counts method invocations. 
2. @Timed: Times calls to a method.  When querying metrics, we will get nice statistics on how long method calls are taking.
3. @Gauge:  An arbitrary metric, up to the developer to decide meaning. 
4. @ConcurrentGauge: counts number of parallel accesses to a particular object. 
5. @Metered: This annotation tracks the frequency of method invocations. 
6. @Metric:  Finally, this annotation can be used to calculate an arbitrary metric. 
## Deployment

Install the JBoss EAP Expansion Pack in the jboss-monitoring-demo namespace.

`oc replace --force -n jboss-monitoring-demo -f https://raw.githubusercontent.com/jboss-container-images/jboss-eap-openshift-templates/eap-xp2/jboss-eap-xp2-openjdk11-openshift.json`

`oc replace --force -n jboss-monitoring-demo -f https://raw.githubusercontent.com/jboss-container-images/jboss-eap-openshift-templates/eap-xp2/templates/eap-xp2-basic-s2i.json`

Deploy the application: 
```
oc new-app --template=eap-xp2-basic-s2i \
 -p APPLICATION_NAME=jboss-monitoring-demo \
 -p EAP_IMAGE_NAME=jboss-eap-xp2-openjdk11-openshift:2.0 \
 -p EAP_RUNTIME_IMAGE_NAME=jboss-eap-xp2-openjdk11-runtime-openshift:2.0 \
 -p IMAGE_STREAM_NAMESPACE=jboss-monitoring-demo \
 -p SOURCE_REPOSITORY_URL=https://github.com/sholly/jboss-monitoring-demo.git \
 -p SOURCE_REPOSITORY_REF=main -p CONTEXT_DIR="."
```


Verify successful build and running pods. 

Create extra service to expose metrics: 

apiVersion: v1
kind: Service
metadata:
  annotations:
  labels:
    app: eap-xp2-basic-s2i-admin
    app.kubernetes.io/component: eap-xp2-basic-s2i-admin
    app.kubernetes.io/instance: eap-xp2-basic-s2i-admin
    application: eap-xp2-basic-app-admin
    template: eap-xp2-basic-s2i-admin
    xpaas: "1.0"
  name: jboss-service-metrics
  namespace: jboss-monitoring-demo
spec:
  ports:
    - name: admin
      port: 9990
      protocol: TCP
      targetPort: 9990
  selector:
    deploymentConfig: jboss-monitoring-demo
  sessionAffinity: None
  type: ClusterIP
status:
  loadBalancer: {}


expose metrics so we can get metrics externally: 
oc expose svc/jboss-service-metrics --path=/metrics

Finally, we need a ServiceMonitor:
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
labels:
k8s-app: prometheus-example-monitor
name: prometheus-example-monitor
namespace: jboss-monitoring-demo
spec:
endpoints:
- interval: 30s
  port: admin
  scheme: http
  selector:
  matchLabels:
  app: eap-xp2-basic-s2i-admin


Apply the one in the openshift directory: 
`oc apply -f openshift/servicemonitor.yaml`


With everything set up, let's generate some traffic and look at metrics:

```shell
for i in {1..500}
do
  curl -k https://jboss-monitoring-demo-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org/jboss-monitoring-demo/prime/9287375; 
done
```

`oc get route` gives us: 

jboss-service-metrics-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org

Check metrics: 

`curl -H "Accept: application/json" jboss-service-metrics-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org/metrics/`

This will give us base, vendor, and application. 

Check application specific metrics: 
`curl -H "Accept: application/json" jboss-service-metrics-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org/metrics/application`

We should get a response like so: 

```json
{
  "checkPrimesTimer": {
    "p99": 1870931.0,
    "min": 94608.0,
    "max": 2228662.0,
    "mean": 252033.13025311296,
    "p50": 174799.0,
    "p999": 2228662.0,
    "stddev": 304817.53102812817,
    "p95": 612241.0,
    "p98": 1713501.0,
    "p75": 224694.0,
    "fiveMinRate": 9.724927709310617E-17,
    "fifteenMinRate": 0.0000018569400895573667,
    "meanRate": 0.02973646274416362,
    "count": 400,
    "oneMinRate": 1.3767065621547969E-80
  },
  "injectedCounter": 0,
  "com.redhat.demo.PrimeChecker.checkPrimesCount": 400,
  "checkPrimesFrequency": {
    "fiveMinRate": 9.725335744431071E-17,
    "fifteenMinRate": 0.0000018569659402847687,
    "meanRate": 0.029736457012208277,
    "count": 400,
    "oneMinRate": 1.377003536411033E-80
  },
  "com.redhat.demo.PrimeChecker.parallelAccess": {
    "current": 10,
    "min": 10,
    "max": 10
  }
}
```
Running the query to return plain text: 
`curl   jboss-service-metrics-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org/metrics/application`

Will give output similar to this: 

```text
...snipped...
# HELP application_checkPrimesTimer_seconds Timing primeCount
# TYPE application_checkPrimesTimer_seconds summary
application_checkPrimesTimer_seconds_count 400.0
application_checkPrimesTimer_seconds{quantile="0.5"} 1.74799E-4
application_checkPrimesTimer_seconds{quantile="0.75"} 2.24694E-4
application_checkPrimesTimer_seconds{quantile="0.95"} 6.12241E-4
application_checkPrimesTimer_seconds{quantile="0.98"} 0.001713501
application_checkPrimesTimer_seconds{quantile="0.99"} 0.001870931
application_checkPrimesTimer_seconds{quantile="0.999"} 0.002228662

```

Go to your openshift web console, choose the Developer view, then click monitoring.  Choose the 'jboss-monitoring-demo' 
namespace, then choose the 'Metrics' tab. 

Choose 'Select Query' -> 'Custom Query'

Pick one of the applcation metrics, like 'application_checkPrimesTimer_seconds', and use this for the custom query.  

## Logging

Use the 'Administrator' perspective -> Projects -> choose 'openshift-logging'. 

Go to routes, then select the 'kibana' route.  

In the query form, use 'message: *prime*' to see calls to checkPrime. 

Use the query 'kubernetes.namespace_name:jboss-monitoring-demo' to see all log messages from the namespace 
jboss-monitoring-demo' 

## Custom Grafana Dashboard

Unfortunately, the grafana instance in the 'openshift-monitoring' namespace is read-only.  It would be nice, however, to 
have a Grafana instance of our own, connected to the openshift-monitoring namespace.  This way we can make custom 
dashboards if we like.  
We will follow this blog post:

https://www.redhat.com/en/blog/custom-grafana-dashboards-red-hat-openshift-container-platform-4

Create a new namespace.  We'll use 'custom-grafana'

Deploy the community version of Grafana into our custom-grafana namespace. 

In the custom-grafana namespace, go to Operators -> Installed Operators, and create a new grafana instance.  

Grant the grafana-serviceaccount access to the cluster role 'cluster-monitoring-view': 

`oc adm policy add-cluster-role-to-user cluster-monitoring-view -z grafana-serviceaccount`

Get the serviceaccount token and save it: 

`oc serviceaccounts get-token grafana-serviceaccount -n my-grafana`

Create a GrafanaDataSource connecting our grafana instance with the thanos-querier instance, replacing ${BEARER_TOKEN}
with our token from the previous step: 

```yaml
apiVersion: integreatly.org/v1alpha1
kind: GrafanaDataSource
metadata:
  name: prometheus-grafanadatasource
  namespace: my-grafana
spec:
  datasources:
    - access: proxy
      editable: true
      isDefault: true
      jsonData:
        httpHeaderName1: 'Authorization'
        timeInterval: 5s
        tlsSkipVerify: true
      name: Prometheus
      secureJsonData:
        httpHeaderValue1: 'Bearer ${BEARER_TOKEN}'
      type: prometheus
      url: 'https://thanos-querier.openshift-monitoring.svc.cluster.local:9091'
  name: prometheus-grafanadatasource.yaml
```

We should now be able to log into our grafana instance and run queries. 



