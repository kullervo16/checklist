/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.service.ChecklistRepository;

/**
 * REST service to expose the checklists via JSON.
 *
 * @author jef
 */
@Path("/checklist")
@Stateless
public class ChecklistService {
    
    
    @EJB
    ChecklistRepository checklistRepository;
    
    
    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist getTemplate(@QueryParam("id") String id) {        
        return this.checklistRepository.getChecklist(id);    
    }
    
    
}
