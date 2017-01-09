DOCKER_CMD="sudo docker"

if [ "${USER}" = "kernixski" ]; then
    DOCKER_CMD="docker"
fi

${DOCKER_CMD} build -t kullervo16/keycloak4cl .
