DOCKER_CMD="sudo docker"

if [ "${USER}" = "kernixski" ]; then
    DOCKER_CMD="docker"
fi

${DOCKER_CMD} stop keycloak
${DOCKER_CMD} rm keycloak

# uncomment if you want to persist your config changes outside your container
#PERSIST="-v /ssd/home/jef/data/keycloak_copy/data:/opt/jboss/keycloak/standalone/data \
#         -v /ssd/home/jef/data/keycloak/log:/opt/jboss/keycloak/standalone/log"

${DOCKER_CMD} run --name keycloak -d            \
                  -e KEYCLOAK_USER=admin        \
                  -e KEYCLOAK_PASSWORD=secret   \
                  -p 8085:8085                  \
                  $PERSIST                      \
                  kullervo16/keycloak4cl        \
                  -Djboss.http.port=8085        \
                  -Djboss.bind.address=0.0.0.0

${DOCKER_CMD} logs -f keycloak