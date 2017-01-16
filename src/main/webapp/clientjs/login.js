


//var keycloakConfig = {
//    "realm": "checklist",
//    "url": "http://keycloak:8085/auth",
//    "ssl-required": "none",
//    "clientId": "checklist",
//    "public-client": true
//};

$.getJSON('KeycloakConfig', function(keycloakConfig) {
    console.log(keycloakConfig);
    window._keycloak = Keycloak(keycloakConfig);

    console.log("Start login process");

    window._keycloak.init({
        onLoad: 'login-required'
    }).success(function () {
        //User has logged in
        console.log("User has successfully logged in");
        angular.bootstrap(document, ['checklist']); // manually bootstrap Angular
    }).error(function () {
        //User has logged in
        console.log("User did not log in like it should");
        alert("Cannot load the application when you are not logged in");
        window.location.reload();
    });
});


