
package kullervo16.checklist.rest;

import java.util.List;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import kullervo16.checklist.messages.PersistenceRequest;
import kullervo16.checklist.model.State;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.ChecklistInfo;
import kullervo16.checklist.model.Step;
import kullervo16.checklist.model.TagcloudEntry;
import kullervo16.checklist.repository.ActorRepository;
import kullervo16.checklist.repository.ChecklistRepository;

/**
 * REST service to expose the checklists via JSON.
 *
 * @author jef
 */
@Path("/checklist")
@Stateless
public class ChecklistService {
    
    // use singleton repository to make sure we are all working on the same backend (@Singleton does not seem to do that job like it should)    
    ChecklistRepository checklistRepository = ChecklistRepository.INSTANCE;
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChecklistInfo> listChecklists(@QueryParam("tag") String tag, @QueryParam("milestone") String milestone) {                
        return this.checklistRepository.getChecklistInformation(tag, milestone);

    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist getCL(@QueryParam("id") String id) {        
        return this.checklistRepository.getChecklist(id);    
    }
    
    @GET
    @Path("/tags/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TagcloudEntry> listTags() {                
        return this.checklistRepository.getTagInfo();

    }
    
    @GET
    @Path("/milestones/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TagcloudEntry> listMilestones() {                
        return this.checklistRepository.getMilestoneInfo();

    }
    
    @POST
    @Path("/setActionResult")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setActionResult(@QueryParam("id") String checklistId, @QueryParam("step") String stepId, @QueryParam("result") boolean result) {        
        Checklist cl =getChecklist(checklistId);
        Step step = getStep(cl, stepId);  
        if(result) {
            if(step.getChecks().isEmpty()) {
                // border case : no checks.. so immediately ok
                step.setState(State.OK);
                verifyCompleteChecklist(cl);
            } else {
                // normal case : checks... only mark executed, the check phase will start
                step.setState(State.EXECUTED);
            }
        } else {
            step.setState(State.EXECUTION_FAILED_NO_COMMENT);
        }
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);
        return cl;
    }
    
    @POST
    @Path("/revalidate")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist revalidate(@QueryParam("id") String checklistId, @QueryParam("step") String stepId) {        
        Checklist cl =getChecklist(checklistId);
        Step step = getStep(cl, stepId);  
        if(State.CHECK_FAILED.equals(step.getState())) {
            // revalidate only allowed when in check failed... otherwise we ignore the request
            step.setState(State.EXECUTED);
            ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);
        }
        
        return cl;
    }
    
    @POST
    @Path("/setCheckResult")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setCheckResult(@QueryParam("id") String checklistId, @QueryParam("step") String stepId, @QueryParam("result") boolean result) {        
        Checklist cl = getChecklist(checklistId);
        Step step = getStep(cl, stepId);  
        if(result) {
            step.setState(State.OK);
            if(step.getMilestone() != null) {
                step.getMilestone().setReached(true);
            }
            verifyCompleteChecklist(cl);
        } else {
            step.setState(State.CHECK_FAILED_NO_COMMENT);
        }
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId),null);         
        return cl;
    }

    private void verifyCompleteChecklist(Checklist cl) throws IllegalArgumentException {
        if(cl.getProgress() == 100) {
            // this checklist is complete... now check whether we are a subchecklist... if so, update the parent
            if(cl.getParent() != null) {
                Checklist parent = getChecklist(cl.getParent());
                for(Step walker : parent.getSteps()) {
                    if(cl.getTemplate().equals(walker.getSubChecklist()) && !walker.isComplete())  {
                        // we update the first not completed step with the proper template (this allows the same template to be used
                        // multiple times as subchecklist in a single instance. We call it recursively because this template may also
                        // have a parent...
                        this.setCheckResult(cl.getParent(), walker.getId(), true);
                    }
                }
            }
        }
    }
    
    
    @POST
    @Path("/addErrorToStep")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist addErrorToStep(@QueryParam("id") String checklistId, @QueryParam("step") String stepId, String error) {        
        Checklist cl = getChecklist(checklistId);
        Step step = getStep(cl, stepId);    
                
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
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);
        return cl;
    }
    
    @POST
    @Path("/addTag")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist addTag(@QueryParam("id") String checklistId, @QueryParam("tag") String tag) {        
        Checklist cl = getChecklist(checklistId);
        if(!cl.getTags().contains(tag)) {
            cl.getTags().add(tag);
            cl.setSpecificTagSet(true);
        }
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);
        return cl;
    }
    
    @POST
    @Path("/setStepOption")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setStepOption(@QueryParam("id") String checklistId, @QueryParam("step") String stepId, @QueryParam("choice") String choice) {        
        Checklist cl =getChecklist(checklistId);
        Step step = getStep(cl, stepId);    
        
        if(step.getSelectedOption() != null && ! choice.equals(step.getSelectedOption())) {
            throw new IllegalStateException("trying to reset the state... for step "+stepId);
        }
        
        step.setSelectedOption(choice);
        step.setState(State.OK);
        if(step.getMilestone() != null) {
            step.getMilestone().setReached(true);
        }
        
        // now iterate all steps to update the once that depend on our choice
        for(Step walker : (List<Step>) cl.getSteps()) {
            if(walker.getCondition() != null) {
                if(walker.getCondition().isConditionUnreachable()) {
                    // this condition has become unreachable.. so set the state to not-applicable.
                    walker.setState(State.NOT_APPLICABLE);
                }
            }
        }
        // can be that the last step in a checklist is not executed... so verify as well
        verifyCompleteChecklist(cl);
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId),null);
        return cl;
    }

    private Checklist getChecklist(String checklistId) throws IllegalArgumentException {
        Checklist cl = (Checklist) this.checklistRepository.getChecklist(checklistId);
        if(cl == null) {
            throw new IllegalArgumentException("No checklist found with id "+checklistId);
        }
        return cl;
    }
    
    private Step getStep(Checklist cl, String stepId) throws IllegalArgumentException {        
        for(Step step : cl.getSteps()) {
            if(step.getId().equals(stepId)) {
                return (Step) step;
            }
        }
        throw new IllegalArgumentException("No step found with id "+stepId);
    }
}
