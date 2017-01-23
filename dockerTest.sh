./dockerBuild.sh

PROJECT_DIR="$(pwd)"
DOCKER_CMD="sudo docker"
KEYCLOAK_SERVER=http://keycloak:8085/auth

if [ "${USER}" = "kernixski" ]; then
    DOCKER_CMD="docker"
    KEYCLOAK_SERVER=http://keycloak:8085/auth
fi

mkdir ${PROJECT_DIR}/target/data

${DOCKER_CMD} stop checklist
${DOCKER_CMD} rm checklist
${DOCKER_CMD} run --name checklist -ti                    \
                  --link keycloak:keycloak                \
                  -p 8084:8080                            \
                  -p 8787:8787                            \
                  -p 9994:9990                            \
                  -v ${PROJECT_DIR}/target/data:/opt/checklist           \
                  -e DEBUG=true                           \
                  -e KEYCLOAK_SERVER=${KEYCLOAK_SERVER}   \
                  checklist:latest

# -v ${PROJECT_DIR}/.deployments:/opt/jboss/wildfly/standalone/deployments
