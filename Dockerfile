FROM jboss/wildfly

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#70365 --silent

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

ADD standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml
ADD index.html /opt/jboss/wildfly/welcome-content/index.html
ADD target/checklist-*.war /opt/jboss/wildfly/standalone/deployments/checklist.war