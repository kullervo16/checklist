
package kullervo16.checklist.rest;

import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.List;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import kullervo16.checklist.messages.PersistenceRequest;
import kullervo16.checklist.model.State;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.ChecklistInfo;
import kullervo16.checklist.model.Step;
import kullervo16.checklist.model.Template;
import kullervo16.checklist.repository.ActorRepository;
import kullervo16.checklist.repository.ChecklistRepository;
import kullervo16.checklist.repository.TemplateRepository;

/**
 * REST service to expose the checklists via JSON.
 *
 * @author jef
 */
@Path("/checklists")
@Stateless
public class ChecklistService {
    
    // use singleton repository to make sure we are all working on the same backend (@Singleton does not seem to do that job like it should)    
    ChecklistRepository checklistRepository = ChecklistRepository.INSTANCE;
    TemplateRepository templateRepository = TemplateRepository.INSTANCE;
    
    @POST
    @Path("/{folder}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String createChecklist(@PathParam("folder") String folder, @PathParam("name") String name,@QueryParam("parent") String parentName, @QueryParam("step") String stepName) throws URISyntaxException {            
        Template template = this.templateRepository.getTemplate(folder, name);
        if(template == null) {
            throw new IllegalArgumentException("Unknown template "+folder+"/"+name);
        }
        if(parentName != null) {
            // update the parent step that launched the subchecklist...
            Checklist parent = this.checklistRepository.getChecklist(parentName);
            for(Step step : parent.getSteps()) {
                if(step.getId().equals(stepName)) {
                    step.setState(State.ON_HOLD);                    
                }
            }
            ActorRepository.getPersistenceActor().tell(new PersistenceRequest(parentName), null);
        }
        return this.checklistRepository.createFromTemplate(folder, name, template, parentName);        
    }
    
    @GET    
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChecklistInfo> listChecklists(@QueryParam("tag") String tag, @QueryParam("milestone") String milestone) {                
        return this.checklistRepository.getChecklistInformation(tag, milestone);

    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist getCL(@PathParam("id") String id) {        
        return this.checklistRepository.getChecklist(id);    
    }
    
    @DELETE
    @Path("/{id}")    
    public Response deleteCL(@PathParam("id") String id) { 
        Checklist cl = this.checklistRepository.getChecklist(id);
        if(cl == null) {
            return Response.status(Response.Status.GONE).build();
        }
        this.checklistRepository.deleteChecklist(cl);
        return Response.ok().build();    
    }
    
    
    @PUT
    @Path("/{id}/{step}/actionresults/{result}")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setActionResult(@PathParam("id") String checklistId, @PathParam("step") String stepId, @PathParam("result") boolean result) {        
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
    
    @PUT
    @Path("/{id}/{step}/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist revalidate(@PathParam("id") String checklistId, @PathParam("step") String stepId) {        
        Checklist cl =getChecklist(checklistId);
        Step step = getStep(cl, stepId);  
        if(State.CHECK_FAILED.equals(step.getState())) {
            // revalidate only allowed when in check failed... otherwise we ignore the request
            step.setState(State.EXECUTED);
            ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);
        }
        
        return cl;
    }
    
    @PUT
    @Path("/{id}/{step}/checkresults/{result}")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setCheckResult(@PathParam("id") String checklistId, @PathParam("step") String stepId, @PathParam("result") boolean result) {        
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
    @Path("/{id}/{step}/errors")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist addErrorToStep(@PathParam("id") String checklistId, @PathParam("step") String stepId, String error) {        
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
    @Path("/{id}/{step}/answers")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist addAnwswerToStep(@PathParam("id") String checklistId, @PathParam("step") String stepId, String answer) {        
        Checklist cl = getChecklist(checklistId);
        Step step = getStep(cl, stepId);            
        
        if(step.getQuestion() != null) {
            if(!step.getChecks().isEmpty()) {                            
                step.setState(State.EXECUTED);
            } else {
                // no checks, directly to OK
                step.setState(State.OK);
            }
            try {
                JsonReader jsonReader = Json.createReader(new StringReader(answer));
                JsonObject object = jsonReader.readObject();
                step.getAnswers().addAll(object.keySet());
                jsonReader.close();
            }catch(Exception e) {
                // single answer...
                step.getAnswers().add(answer);    
            }
            ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);
        }
        return cl;
    }
    
    
    
    @PUT
    @Path("/{id}/tags/{tag}")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist addTag(@PathParam("id") String checklistId, @PathParam("tag") String tag) {        
        Checklist cl = getChecklist(checklistId);
        if(!cl.getTags().contains(tag)) {
            cl.getTags().add(tag);
            cl.setSpecificTagSet(true);
        }
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);
        return cl;
    }
    
    @DELETE
    @Path("/{id}/tags/{tag}")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist removeTag(@PathParam("id") String checklistId, @PathParam("tag") String tag) {        
        Checklist cl = getChecklist(checklistId);
        cl.getTags().remove(tag);
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);
        return cl;
    }
    
    @PUT
    @Path("/{id}/{step}/options/{choice}")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setStepOption(@PathParam("id") String checklistId, @PathParam("step") String stepId, @PathParam("choice") String choice) {        
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
