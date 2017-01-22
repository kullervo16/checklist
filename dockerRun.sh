PROJECT_DIR="$(pwd)"
DATA_DIR="/home/jef/data/checklist"
DOCKER_CMD="sudo docker"
KEYCLOAK_SERVER=http://keycloak:8085/auth

if [ "${USER}" = "kernixski" ]; then
    DATA_DIR="${PROJECT_DIR}/.local/data"
    DOCKER_CMD="docker"
    KEYCLOAK_SERVER=http://keycloak:8085/auth
fi

${DOCKER_CMD} stop checklist
${DOCKER_CMD} rm checklist
${DOCKER_CMD} run --name checklist -ti                    \
                  --link keycloak:keycloak                \
                  -p 8084:8080                            \
                  -p 8787:8787                            \
                  -p 9994:9990                            \
                  -v ${DATA_DIR}:/opt/checklist           \
                  -e DEBUG=true                           \
                  -e KEYCLOAK_SERVER=${KEYCLOAK_SERVER}   \
                  checklist:latest

# -v ${PROJECT_DIR}/.deployments:/opt/jboss/wildfly/standalone/deployments
