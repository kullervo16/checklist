/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.rest;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import kullervo16.checklist.model.Template;
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
    public List<String> listTemplateNames() {
        System.err.println("########################"+this.templateRepository);
        return this.templateRepository.getTemplateNames();

    }
    
    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Template getTemplate() {
        return new Template();

    }
}
