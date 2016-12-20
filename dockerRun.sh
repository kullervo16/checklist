sudo docker stop checklist
sudo docker rm checklist
sudo docker run -ti -p 8084:8080 -p 9994:9990 --name checklist --link keycloak:keycloak -v /home/jef/data/checklist:/opt/checklist checklist:latest
