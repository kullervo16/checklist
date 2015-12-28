/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.State;
import kullervo16.checklist.model.Step;
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
    
    @POST
    @Path("/setActionResult")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setActionResult(@QueryParam("id") String checklistId, @QueryParam("step") String stepId, @QueryParam("result") boolean result) {        
        Checklist cl = this.checklistRepository.getChecklist(checklistId);  
        for(Step step : cl.getSteps()) {
            if(step.getId().equals(stepId)) {
                if(result) {
                    step.setState(State.EXECUTED);
                } else {
                    step.setState(State.EXECUTION_FAILED);
                }
            }
        }
        return cl;
    }
    
}
