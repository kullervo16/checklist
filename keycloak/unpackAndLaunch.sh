#!/bin/bash

if [ ! -f /opt/jboss/keycloak/standalone/exported ]; then
    echo "First time... expand checklist config"
    cd /opt/jboss/keycloak/standalone
    tar -vxzf /opt/checklist/data.tgz
    touch /opt/jboss/keycloak/standalone/exported
fi

cd /opt/jboss
./docker-entrypoint.sh $@

