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


With everything set up, let's look at metrics: 

`oc get route` gives us: 

jboss-service-metrics-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org

Check metrics: 

`curl -H "Accept: application/json" jboss-service-metrics-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org/metrics/`

This will give us base, vendor, and application. 

Check application specific metrics: 
`curl -H "Accept: application/json" jboss-service-metrics-jboss-monitoring-demo.apps.ocp4.lab.unixnerd.org/metrics/`



