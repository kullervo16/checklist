/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.rest;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import kullervo16.checklist.model.Template;
import kullervo16.checklist.model.TemplateInfo;
import kullervo16.checklist.service.TemplateRepository;

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
}
