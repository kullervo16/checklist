/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.rest;

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
import kullervo16.checklist.model.Template;
import kullervo16.checklist.model.TemplateInfo;
import kullervo16.checklist.repository.ActorRepository;
import kullervo16.checklist.repository.ChecklistRepository;
import kullervo16.checklist.repository.TemplateRepository;

/**
 * REST service to expose the templates via JSON.
 *
 * @author jef
 */
@Path("/template")
@Stateless
public class TemplateService {
    
    // use singleton repository to make sure we are all working on the same backend (@Singleton does not seem to do that job like it should)    
    TemplateRepository templateRepository = TemplateRepository.INSTANCE;
    
    
    ChecklistRepository checklistRepository = ChecklistRepository.INSTANCE;
        
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
    @Produces(MediaType.TEXT_PLAIN)
    public String createChecklist(@QueryParam("id") String id, @QueryParam("parent") String parent) throws URISyntaxException {    
        Template template = this.templateRepository.getTemplate(id);
        if(template == null) {
            throw new IllegalArgumentException("Unknown template "+id);
        }
        
        return this.checklistRepository.createFromTemplate(id, template, parent);        
    }
}
