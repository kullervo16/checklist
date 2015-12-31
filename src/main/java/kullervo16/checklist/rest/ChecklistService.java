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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.State;
import kullervo16.checklist.model.Step;
import kullervo16.checklist.model.dto.ChecklistDto;
import kullervo16.checklist.model.dto.StepDto;
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
        ChecklistDto cl =getChecklist(checklistId);
        StepDto step = getStep(cl, stepId);  
        if(result) {
            step.setState(State.EXECUTED);
        } else {
            step.setState(State.EXECUTION_FAILED_NO_COMMENT);
        }
        return cl;
    }
    
    @POST
    @Path("/setCheckResult")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setCheckResult(@QueryParam("id") String checklistId, @QueryParam("step") String stepId, @QueryParam("result") boolean result) {        
        ChecklistDto cl =getChecklist(checklistId);
        StepDto step = getStep(cl, stepId);  
        if(result) {
            step.setState(State.OK);
            if(step.getMilestone() != null) {
                step.getMilestone().setReached(true);
            }
        } else {
            step.setState(State.CHECK_FAILED_NO_COMMENT);
        }
                 
        return cl;
    }
    
    
    @POST
    @Path("/addErrorToStep")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist addErrorToStep(@QueryParam("id") String checklistId, @QueryParam("step") String stepId, String error) {        
        ChecklistDto cl =getChecklist(checklistId);
        StepDto step = getStep(cl, stepId);    
                
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
        return cl;
    }
    
    @POST
    @Path("/addTag")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist addTag(@QueryParam("id") String checklistId, @QueryParam("tag") String tag) {        
        ChecklistDto cl = getChecklist(checklistId);
        if(!cl.getTags().contains(tag)) {
            cl.getTags().add(tag);
            cl.setSpecificTagSet(true);
        }
        return cl;
    }
    
    @POST
    @Path("/setStepOption")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setStepOption(@QueryParam("id") String checklistId, @QueryParam("step") String stepId, @QueryParam("choice") String choice) {        
        ChecklistDto cl =getChecklist(checklistId);
        StepDto step = getStep(cl, stepId);    
        
        if(step.getSelectedOption() != null && ! choice.equals(step.getSelectedOption())) {
            throw new IllegalStateException("trying to reset the state... for step "+stepId);
        }
        
        step.setSelectionOption(choice);
        step.setState(State.OK);
        if(step.getMilestone() != null) {
            step.getMilestone().setReached(true);
        }
        
        // now iterate all steps to update the once that depend on our choice
        for(StepDto walker : (List<StepDto>) cl.getSteps()) {
            if(walker.getCondition() != null) {
                if(walker.getCondition().isConditionUnreachable()) {
                    // this condition has become unreachable.. so set the state to not-applicable.
                    walker.setState(State.NOT_APPLICABLE);
                }
            }
        }
        
        return cl;
    }

    private ChecklistDto getChecklist(String checklistId) throws IllegalArgumentException {
        ChecklistDto cl = (ChecklistDto) this.checklistRepository.getChecklist(checklistId);
        if(cl == null) {
            throw new IllegalArgumentException("No checklist found with id "+checklistId);
        }
        return cl;
    }
    
    private StepDto getStep(ChecklistDto cl, String stepId) throws IllegalArgumentException {        
        for(Step step : cl.getSteps()) {
            if(step.getId().equals(stepId)) {
                return (StepDto) step;
            }
        }
        throw new IllegalArgumentException("No step found with id "+stepId);
    }
}
