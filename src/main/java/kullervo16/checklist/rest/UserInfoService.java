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

   
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map getUserInfo(@Context SecurityContext context) {
        Map result = new HashMap();
        if(context == null || context.getUserPrincipal() == null) {
            result.put("userName", "unknown");        
            result.put("name", "unknown");
            result.put("roles", new LinkedList());
            
        } else {
            if(context.getUserPrincipal() instanceof KeycloakPrincipal) {
                KeycloakPrincipal<KeycloakSecurityContext> kp = (KeycloakPrincipal<KeycloakSecurityContext>)  context.getUserPrincipal();

                  // this is how to get the real userName (or rather the login name)
                  result.put("userName",kp.getKeycloakSecurityContext().getIdToken().getPreferredUsername());
                  result.put("name", kp.getKeycloakSecurityContext().getIdToken().getName());
                  result.put("roles", kp.getKeycloakSecurityContext().getToken().getRealmAccess().getRoles());
            } else {
                result.put("userName",context.getUserPrincipal().getName());
                result.put("name", "unknown"); 
                List<String> roles = new LinkedList<>();
                roles.add("admin");
                roles.add("modify");
                roles.add("consult");
                result.put("roles", roles); // no generic way to get the roles based on this information... so add them all for the GUI to show, the backend will refuse when needed 
            }
        }
        
        return result;
    }
    
    public String getUserName(SecurityContext context) {
        return (String) this.getUserInfo(context).get("name");
    }
}
