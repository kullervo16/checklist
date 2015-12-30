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
                    step.setState(State.EXECUTION_FAILED_NO_COMMENT);
                }
            }
        }
        return cl;
    }
    
    @POST
    @Path("/setCheckResult")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setCheckResult(@QueryParam("id") String checklistId, @QueryParam("step") String stepId, @QueryParam("result") boolean result) {        
        Checklist cl = this.checklistRepository.getChecklist(checklistId);  
        for(Step step : cl.getSteps()) {
            if(step.getId().equals(stepId)) {
                if(result) {
                    step.setState(State.OK);
                } else {
                    step.setState(State.CHECK_FAILED_NO_COMMENT);
                }
            }
        }
        return cl;
    }
    
    
    @POST
    @Path("/addErrorToStep")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist addErrorToStep(@QueryParam("id") String checklistId, @QueryParam("step") String stepId, String error) {        
        Checklist cl = this.checklistRepository.getChecklist(checklistId);  
        for(Step step : cl.getSteps()) {
            if(step.getId().equals(stepId)) {                
                switch (step.getState()) {
                    case EXECUTION_FAILED_NO_COMMENT:
                        step.setState(State.EXECUTION_FAILED);
                        break;
                    case CHECK_FAILED_NO_COMMENT:
                        step.setState(State.CHECK_FAILED);
                        break;
                    default:
                        throw new IllegalStateException("Current step state "+step.getState()+" does not permit adding errors");
                }
                step.getErrors().add(error);
            }
        }
        return cl;
    }
}
