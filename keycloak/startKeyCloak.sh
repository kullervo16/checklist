sudo docker stop keycloak
sudo docker rm keycloak

# uncomment if you want to persist your config changes outside your container
#export PERSIST="-v /ssd/home/jef/data/keycloak_copy/data:/opt/jboss/keycloak/standalone/data \
#                -v /ssd/home/jef/data/keycloak/log:/opt/jboss/keycloak/standalone/log"

sudo docker run -d -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=secret \
     --name keycloak -p 8085:8085 $PERSIST \
     kullervo16/keycloak4cl \
     -Djboss.http.port=8085 -Djboss.bind.address=0.0.0.0  # if you want to load it on another port than 8080, specify via these startup params
sudo docker logs -f keycloak


# uncomment and map to a location on your host to make it persistent
     