FROM jboss/keycloak:2.4.0.Final
USER root
COPY data.tgz /opt/checklist/
ADD unpackAndLaunch.sh /opt/checklist
RUN chmod +x /opt/checklist/*.sh && chown -R jboss.jboss /opt/checklist
USER jboss
ENTRYPOINT  ["/opt/checklist/unpackAndLaunch.sh"]
CMD []