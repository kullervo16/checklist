
package kullervo16.checklist.rest;

import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonReader;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.ChecklistInfo;
import kullervo16.checklist.model.Milestone;
import kullervo16.checklist.model.State;
import kullervo16.checklist.model.Step;
import kullervo16.checklist.model.TagcloudEntry;
import kullervo16.checklist.model.Template;
import kullervo16.checklist.repository.ActorRepository;
import kullervo16.checklist.repository.ChecklistRepository;
import kullervo16.checklist.repository.TemplateRepository;

import static kullervo16.checklist.model.State.CHECK_FAILED;
import static kullervo16.checklist.model.State.CHECK_FAILED_NO_COMMENT;
import static kullervo16.checklist.model.State.EXECUTED;
import static kullervo16.checklist.model.State.EXECUTION_FAILED;
import static kullervo16.checklist.model.State.EXECUTION_FAILED_NO_COMMENT;
import static kullervo16.checklist.model.State.OK;
import static kullervo16.checklist.model.State.UNKNOWN;
import static kullervo16.checklist.utils.StringUtils.nullifyAndTrim;

/**
 * REST service to expose the checklists via JSON.
 *
 * @author jef
 */
@Path("/checklists")
@Stateless
public class ChecklistService {

    // use singleton repository to make sure we are all working on the same backend (@Singleton does not seem to do that job like it should)    
    private final ChecklistRepository checklistRepository = ChecklistRepository.INSTANCE;

    private final TemplateRepository templateRepository = TemplateRepository.INSTANCE;


    @POST
    @Path("/{folder}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String createChecklist(@PathParam("folder") final String folder, @PathParam("name") final String name, @QueryParam("parent") final String parentName, @QueryParam("step") final String stepName) throws URISyntaxException {

        final Template template = templateRepository.getTemplate(folder, name);

        if (template == null) {
            throw new IllegalArgumentException("Unknown template " + folder + '/' + name);
        }

        final String childUUID = checklistRepository.createFromTemplate(folder, name, template, parentName);

        if (parentName != null) {

            // update the parent step that launched the subchecklist...

            final Checklist parent = checklistRepository.getChecklist(parentName);

            for (final Step step : parent.getSteps()) {

                if (step.getId().equals(stepName)) {
                    parent.updateStepState(step, State.IN_PROGRESS);
                    step.setChild(childUUID);
                }
            }

            ActorRepository.getPersistenceActor().tell(new PersistenceRequest(parentName), null);
        }

        return childUUID;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChecklistInfo> listChecklists(@QueryParam("tags") final String tags,
                                              @QueryParam("milestones") final String milestones,
                                              @QueryParam("showSubChecklists") @DefaultValue("false") String showSubChecklistsParam) {

        List<String> tagList = null;
        List<String> milestoneList = null;
        final boolean showSubChecklists;

        if (tags != null) {
            tagList = Arrays.asList(tags.split(","));
        }

        if (milestones != null) {
            milestoneList = Arrays.asList(milestones.split(","));
        }

        if (showSubChecklistsParam != null) {
            showSubChecklistsParam = showSubChecklistsParam.trim();
        }

        showSubChecklists = "true".equalsIgnoreCase(showSubChecklistsParam)
                            || "1".equalsIgnoreCase(showSubChecklistsParam);

        return checklistRepository.getChecklistInformation(tagList, milestoneList, !showSubChecklists);
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist getCL(@PathParam("id") final String id) {
        return checklistRepository.getChecklist(id);
    }


    @DELETE
    @Path("/{id}")
    public Response deleteCL(@PathParam("id") final String id) {

        final Checklist cl = checklistRepository.getChecklist(id);

        if (cl == null) {
            return Response.status(Response.Status.GONE).build();
        }

        checklistRepository.deleteChecklist(cl);

        return Response.ok().build();
    }


    @POST
    @Path("/{id}/actions/close")
    public Response closeCL(@PathParam("id") final String id) {

        final Checklist cl = checklistRepository.getChecklist(id);

        cl.close();
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(id), null);

        return Response.ok().build();
    }


    @PUT
    @Path("/{id}/{step}/actionresults/{result}")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setActionResult(@PathParam("id") final String checklistId, @PathParam("step") final String stepId, @PathParam("result") final boolean result) {

        final Checklist cl = getChecklist(checklistId);
        final Step step = getStep(cl, stepId);

        cl.updateStepState(step, result ? step.getChecks().isEmpty() ? OK : EXECUTED
                                        : EXECUTION_FAILED_NO_COMMENT);

        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);

        return cl;
    }


    @PUT
    @Path("/{id}/{step}/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist revalidate(@PathParam("id") final String checklistId, @PathParam("step") final String stepId) {

        final Checklist cl = getChecklist(checklistId);
        final Step step = getStep(cl, stepId);

        // revalidate only allowed when in check failed... otherwise we ignore the request
        if (CHECK_FAILED.equals(step.getState())) {
            cl.updateStepState(step, EXECUTED);
            ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);
        }

        return cl;
    }


    @PUT
    @Path("/{id}/{step}/reopen")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist reopen(@PathParam("id") final String checklistId, @PathParam("step") final String stepId) {

        final Checklist cl = getChecklist(checklistId);
        final Step step = getStep(cl, stepId);

        cl.updateStepState(step, UNKNOWN);
        step.setAnswers(new LinkedList<>());
        step.setErrors(new LinkedList<>());
        step.setSelectedOption(null);

        // Remove the milestone if any
        {
            final Milestone milestone = step.getMilestone();

            if (milestone != null) {
                milestone.setReached(false);
            }
        }

        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);

        return cl;
    }


    @PUT
    @Path("/{id}/{step}/checkresults/{result}")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setCheckResult(@PathParam("id") final String checklistId, @PathParam("step") final String stepId, @PathParam("result") final boolean result) {

        final Checklist cl = getChecklist(checklistId);

        cl.updateStepState(getStep(cl, stepId), result ? OK : CHECK_FAILED_NO_COMMENT);
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);

        return cl;
    }


    @POST
    @Path("/{id}/{step}/errors")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist addErrorToStep(@PathParam("id") final String checklistId, @PathParam("step") final String stepId, final String error) {

        final Checklist cl = getChecklist(checklistId);
        final Step step = getStep(cl, stepId);

        switch (step.getState()) {

            case EXECUTION_FAILED_NO_COMMENT:
                cl.updateStepState(step, EXECUTION_FAILED);
                break;

            case CHECK_FAILED_NO_COMMENT:
                cl.updateStepState(step, CHECK_FAILED);
                break;

            default:
                throw new IllegalStateException("Current step state " + step.getState() + " does not permit adding errors");
        }

        step.getErrors().add(error);
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);

        return cl;
    }


    @POST
    @Path("/{id}/{step}/answers")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist addAnwswerToStep(@PathParam("id") final String checklistId, @PathParam("step") final String stepId, final String answer) {

        if (answer == null || "".equals(answer)) {
            throw new IllegalArgumentException("The answer should be non-empty");
        }

        final Checklist cl = getChecklist(checklistId);
        final Step step = getStep(cl, stepId);

        if (step.getQuestion() != null) {

            cl.updateStepState(step, step.getChecks().isEmpty() ? OK : EXECUTED);

            try {
                final JsonReader jsonReader = Json.createReader(new StringReader(answer));
                step.getAnswers().addAll(jsonReader.readObject().keySet());
                jsonReader.close();
            } catch (final Exception e) {
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
    public Response addTag(@PathParam("id") final String checklistId, @PathParam("tag") final String tagCandidate) {

        final Checklist cl = getChecklist(checklistId);

        // see if a similar tag already exists.. if so, replace it with that tag to get more matches (and better tagclouds)
        String tag = nullifyAndTrim(tagCandidate);

        // Only do something if the client provided a tagCandidate
        if (tagCandidate != null) {

            if (tagCandidate.equalsIgnoreCase("subchecklist")) {
                throw new IllegalArgumentException(tagCandidate + " is a forbidden tag");
            }

            for (final TagcloudEntry te : checklistRepository.getTagInfo(null).getEntries()) {

                if (te.getText().equalsIgnoreCase(tagCandidate)) {
                    tag = te.getText();
                }
            }

            if (!cl.getTags().contains(tag)) {

                final List<String> newTagList = new LinkedList<>(cl.getTags());

                newTagList.add(tag);
                cl.setUniqueTagcombination(checklistRepository.isTagCombinationUnique(newTagList, cl.getId()));
                cl.getTags().add(tag);
                cl.setSpecificTagSet(true);
            }

            ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);
        }

        return Response.status(Response.Status.OK).entity(cl).build();
    }


    @DELETE
    @Path("/{id}/tags/{tag}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeTag(@PathParam("id") final String checklistId, @PathParam("tag") final String tag) {

        final Checklist cl = getChecklist(checklistId);

        // Prevent the user to delete the subchecklist tag
        if (tag.equals("subchecklist")){
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"error\":\"You cannot delete the subchecklist tag\"}").build();
        }

        // check that the tag is checklist specific, don't delete tags from the template...
        {
            final String templateId = cl.getTemplate();
            final Template template = templateRepository.getTemplate(templateId);
            final String[] tagsFromTemplateId = Checklist.getTagsFromTemplateId(templateId);

            if (template.getTags().contains(tag) || tag.equals(tagsFromTemplateId[0]) || tag.equals(tagsFromTemplateId[1])) {
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity("{\"error\":\"You cannot delete a tag defined by the template\"}").build();
            }
        }

        final List<String> newTagList = new LinkedList<>(cl.getTags());

        newTagList.remove(tag);
        cl.setUniqueTagcombination(checklistRepository.isTagCombinationUnique(newTagList, cl.getId()));
        cl.getTags().remove(tag);
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);

        return Response.status(Response.Status.OK).entity(cl).build();
    }


    @PUT
    @Path("/{id}/{step}/options/{choice}")
    @Produces(MediaType.APPLICATION_JSON)
    public Checklist setStepOption(@PathParam("id") final String checklistId, @PathParam("step") final String stepId, @PathParam("choice") final String choice) {

        final Checklist cl = getChecklist(checklistId);
        final Step step = getStep(cl, stepId);

        if (step.getSelectedOption() != null && !choice.equals(step.getSelectedOption())) {
            throw new IllegalStateException("trying to reset the state... for step " + stepId);
        }

        step.setSelectedOption(choice);
        cl.updateStepState(step, OK);
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(checklistId), null);

        return cl;
    }


    private Checklist getChecklist(final String checklistId) throws IllegalArgumentException {

        final Checklist cl = checklistRepository.getChecklist(checklistId);

        if (cl == null) {
            throw new IllegalArgumentException("No checklist found with id " + checklistId);
        }

        return cl;
    }


    private Step getStep(final Checklist cl, final String stepId) throws IllegalArgumentException {

        for (final Step step : cl.getSteps()) {
            if (step.getId().equals(stepId)) {
                return step;
            }
        }

        throw new IllegalArgumentException("No step found with id " + stepId);
    }
}
