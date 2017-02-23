/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.rest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;

/**
 * REST Web Service
 *
 * @author jef
 */
@Path("/userInfo")
@RequestScoped
public class UserInfoService {

    public static final String USER_ID_PROPERTY_NAME = "userName";

    public static final String USER_NAME_PROPERTY_NAME = "name";


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map getUserInfo(@Context SecurityContext context) {
        Map result = new HashMap();
        if(context == null || context.getUserPrincipal() == null) {
            result.put(USER_ID_PROPERTY_NAME, "unknown");
            result.put(USER_NAME_PROPERTY_NAME, "unknown");
            result.put("roles", new LinkedList());

        } else {
            if(context.getUserPrincipal() instanceof KeycloakPrincipal) {
                KeycloakPrincipal<KeycloakSecurityContext> kp = (KeycloakPrincipal<KeycloakSecurityContext>)  context.getUserPrincipal();

                  // this is how to get the real userName (or rather the login name)
                  if(kp.getKeycloakSecurityContext().getIdToken() != null) {
                      // webapp style login
                      result.put(USER_ID_PROPERTY_NAME,kp.getKeycloakSecurityContext().getIdToken().getPreferredUsername());
                      result.put(USER_NAME_PROPERTY_NAME, kp.getKeycloakSecurityContext().getIdToken().getName());
                  } else {
                      // oAuth2 style
                      result.put(USER_ID_PROPERTY_NAME,kp.getKeycloakSecurityContext().getToken().getPreferredUsername());
                      result.put(USER_NAME_PROPERTY_NAME, kp.getKeycloakSecurityContext().getToken().getName());
                  }
                  result.put("roles", kp.getKeycloakSecurityContext().getToken().getRealmAccess().getRoles());
            } else {
                result.put(USER_ID_PROPERTY_NAME,context.getUserPrincipal().getName());
                result.put(USER_NAME_PROPERTY_NAME, "unknown");
                List<String> roles = new LinkedList<>();
                roles.add("admin");
                roles.add("modify");
                roles.add("consult");
                result.put("roles", roles); // no generic way to get the roles based on this information... so add them all for the GUI to show, the backend will refuse when needed 
            }
        }

        return result;
    }

    public String getUserName(final SecurityContext context) {
        return getUserName(this.getUserInfo(context));
    }

    public String getUserName(final Map<String,String> userProperties) {
        return (String) userProperties.get(USER_NAME_PROPERTY_NAME);
    }

    public String getUserId(final Map<String,String> userProperties) {
        return (String) userProperties.get(USER_ID_PROPERTY_NAME);
    }
}
