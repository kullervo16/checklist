# User management

## Keycloak ?

For the checklist usermanagement we use [Keycloak](http://www.keycloak.org/). This provides a complete user management solution.... important for us are 

* authentication : built-in or via external identity provider (IDP), the keycloak server will make sure you are who you say you are
* authorization : the role based access is implemented using standards (OAuth2) and directly integrated into both frontend and backend technology
* user self-service : the user can edit his account, the server handles email-verification and password recovery (i.e. all the nasty stuff we don't want to write ourselves)
* admin control : you control which users you give which roles, see who is logged on, can activate logs,... again, all those features you would require from us that we happily skip implementing.


## How to setup.

The general setup can be found in the [keycloak documentation](https://keycloak.gitbooks.io/getting-started-tutorials/content/v/2.4/index.html). However, to make it a bit easier, we provide
an [out-of-the-box docker image](https://hub.docker.com/r/kullervo16/keycloak4cl/) with keycloak already configured for basic usage

This includes :
* 3 typical users
  - Cyril is a consulting user... he can only see what others are doing with the application
  - Marc has modify rights... he can create checklists and execute them
  - Alice is the Admin... she can create/modify templates, reorganize the tags (she of course has consult and modify rights as well)
* a link for your local installation on localhost:8080
* default user settings where users can register themselves (without email verification). New users get the consult and modify role.

When deploying to anythin other than localhost, you should add the URL in the checklist client (of the checklist realm). When starting the application, you pass the location from your keycloak 
server via the environment variable **KEYCLOAK_SERVER**... and that's it.