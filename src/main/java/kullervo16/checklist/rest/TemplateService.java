/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import kullervo16.checklist.model.Template;
import kullervo16.checklist.model.TemplateInfo;
import kullervo16.checklist.service.TemplateRepository;
import org.jboss.resteasy.core.ServerResponse;

/**
 * REST service to expose the templates via JSON.
 *
 * @author jef
 */
@Path("/template")
@Stateless
public class TemplateService {
    
    @EJB
    TemplateRepository templateRepository;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TemplateInfo> listTemplateNames() {                
        return this.templateRepository.getTemplateInformation();

    }
    
    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Template getTemplate(@QueryParam("id") String id) {        
        return this.templateRepository.getTemplate(id);    
    }
    
    @POST
    @Path("/createChecklist")
    @Produces(MediaType.APPLICATION_JSON)
    public String createChecklist(@QueryParam("id") String id) throws URISyntaxException {        
        return "\"checklist.html?id=boe\"";        
    }
}
