Install the JBoss EAP Expansion Pack in the jboss-monitoring-demo namespace.

oc replace --force -n jboss-monitoring-demo -f https://raw.githubusercontent.com/jboss-container-images/jboss-eap-openshift-templates/eap-xp2/jboss-eap-xp2-openjdk11-openshift.json

oc replace --force -n jboss-monitoring-demo -f https://raw.githubusercontent.com/jboss-container-images/jboss-eap-openshift-templates/eap-xp2/templates/eap-xp2-basic-s2i.json

oc new-app --template=eap-xp2-basic-s2i -p APPLICATION_NAME=eap-demo -p EAP_IMAGE_NAME=jboss-eap-xp2-openjdk11-openshift:2.0 -p EAP_RUNTIME_IMAGE_NAME=jboss-eap-xp2-openjdk11-runtime-openshift:2.0 -p IMAGE_STREAM_NAMESPACE=openshift -p SOURCE_REPOSITORY_URL=https://github.com/sholly
