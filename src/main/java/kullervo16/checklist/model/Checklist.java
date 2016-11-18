package kullervo16.checklist.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kullervo16.checklist.messages.PersistenceRequest;
import kullervo16.checklist.model.persist.ChecklistPersister;
import kullervo16.checklist.repository.ActorRepository;
import kullervo16.checklist.repository.ChecklistRepository;

import static kullervo16.checklist.model.State.ABORTED;
import static kullervo16.checklist.model.State.EXECUTION_FAILED;
import static kullervo16.checklist.model.State.OK;

/**
 * Data object class to model a Checklist... it is backed by a YAML file.
 *
 * @author jeve
 */
public class Checklist extends Template {

    private static final Pattern SLASH_PATTERN = Pattern.compile("/");

    private boolean hasSpecificTags;

    private String parent;

    private String template;

    private boolean uniqueTagcombination;


    public Checklist() {
    }


    public Checklist(final File file) {
        persister = new ChecklistPersister(file, this);
    }


    public Checklist(final String uuid, final Template template, final File file, final Checklist parent) {

        this(file);

        id = uuid;
        description = template.getDescription();
        displayName = template.getDisplayName();
        this.template = template.getId();

        // deep copy... we're working completely in memory here, don't want to link references...  

        // Add the tags from the template
        tags = new LinkedList<>(template.getTags());

        // Add the template ID to the tag list if it is not yet a tag
        {
            final String[] templateIdTags = getTagsFromTemplateId(this.template);

            if (!tags.contains(templateIdTags[0])) {
                tags.add(templateIdTags[0]);
            }

            if (!tags.contains(templateIdTags[1])) {
                tags.add(templateIdTags[1]);
            }
        }

        milestones = new LinkedList<>();
        steps = new LinkedList<>();

        for (final Step step : template.getSteps()) {

            final Step newStep = new Step(step, steps);

            steps.add(newStep);

            if (newStep.getMilestone() != null) {
                // recreate the milestone list from the steps (needs to point to the same instance to allow proper update when the step is updated)
                milestones.add(newStep.getMilestone());
            }
        }

        if (parent != null) {

            // Add the subchecklist tag to identify sub-checklists
            tags.add("subchecklist");

            final String[] parentTtemplateIdTags = getTagsFromTemplateId(parent.template);

            // Add parent's tags to the sub-checklist
            for (final String parentTag : parent.getTags()) {

                // Do not add existing tags
                // Do not add the tag generated tags based on the template ID to keep tags set unique
                if (!tags.contains(parentTag) && !parentTag.equals(parentTtemplateIdTags[0]) && !parentTag.equals(parentTtemplateIdTags[1])) {
                    tags.add(parentTag);
                }
            }

            hasSpecificTags = true;
            this.parent = parent.getId();

        } else {
            hasSpecificTags = false;
        }
    }


    @JsonIgnore
    public boolean isComplete() {

        for (final Step step : getSteps()) {
            if (!step.isComplete()) {
                // there is at least 1 not yet executed step in the list
                return false;
            }
        }

        return true;
    }


    /**
     * This method calculates the worst state of the
     *
     * @return
     */
    @JsonIgnore
    public State getState() {

        State aggregatedState = State.UNKNOWN;

        for (final Step step : getSteps()) {
            if (step.getState().compareTo(aggregatedState) > 0) {
                aggregatedState = step.getState();
            }
        }

        return aggregatedState;
    }


    /**
     * This method returns a percentage of progress
     *
     * @return
     */
    public int getProgress() {

        final List<? extends Step> stepWalker = steps;
        int totalSteps = 0;
        int stepsToDo = 0;

        for (final Step step : stepWalker) {

            if (!step.isComplete()) {
                stepsToDo += step.getWeight();
            }

            totalSteps += step.getWeight();
        }

        if (stepsToDo == 0) {
            return 100; // prevent rounding issues (complete should be 100%, not 99.999 :-) )
        }

        return (int) ((totalSteps - stepsToDo) / (totalSteps * 0.01));
    }


    public void setProgress(final int progress) {
        // ignore, progress is calculated, but otherwise JAX-RS complains (however, we want it in the JSON for our angular client).
    }


    public boolean isSpecificTagSet() {
        return hasSpecificTags;
    }


    public void setSpecificTagSet(final boolean hasSpecificTags) {
        this.hasSpecificTags = hasSpecificTags;
    }


    public String getParent() {
        checkAndLoadDataFromFile();
        return parent;
    }


    public void setParent(final String parent) {
        this.parent = parent;
    }


    public String getTemplate() {
        checkAndLoadDataFromFile();
        return template;
    }


    public void setTemplate(final String template) {
        this.template = template;
    }


    public void setUniqueTagcombination(final boolean tagCombinationUnique) {
        uniqueTagcombination = tagCombinationUnique;
    }


    public boolean isUniqueTagcombination() {
        return uniqueTagcombination;
    }


    public static String[] getTagsFromTemplateId(final String templateId) {

        // Remove the leading '/' split id based on '/'
        return SLASH_PATTERN.split(templateId.substring(1));
    }


    public boolean isSubchecklist() {
        return this.parent != null;
    }


    public void updateStepState(final Step step, final State state) {

        final State previousState = step.state;
        final Condition condition = step.getCondition();

        step.setState(state);

        if (state == OK) {

            if (step.getMilestone() != null) {
                step.getMilestone().setReached(true);
            }
        }

        // If the state completion has changed
        if (previousState == null || previousState.isComplete() != state.isComplete()) {

            // Update dependent steps
            for (int stepPos = this.steps.size() - 1; stepPos >= 0; stepPos--) {

                final Step stepWalker = this.steps.get(stepPos);
                final Condition stepWalkerCondition = stepWalker.getCondition();

                if (stepWalkerCondition != null && stepWalkerCondition.getStep() == step) {

                    if (stepWalkerCondition.isConditionReachable()) {
                        updateStepState(stepWalker, State.UNKNOWN);
                    } else {
                        updateStepState(stepWalker, stepWalkerCondition.getSelectedOption() == null ? State.NOT_YET_APPLICABLE : State.NOT_APPLICABLE);
                    }
                }

                if (stepWalker == step) {
                    break;
                }
            }

            updateStepReopenable(step);

            // Update reopenable for step this step depends on, if any
            if (condition != null) {

                updateStepReopenable(condition.getStep());
            }

            if (state.isComplete()) {

                // If the checklist is complete
                if (getProgress() == 100) {

                    // If it is a sub-checklist, update the parent checklist step pointing on this checklist
                    if (this.parent != null) {

                        final Checklist parentCl = ChecklistRepository.INSTANCE.getChecklist(this.parent);

                        if (parentCl != null) {

                            for (final Step parentClStepsWalker : parentCl.getSteps()) {

                                if (this.template.equals(parentClStepsWalker.getSubChecklist()) && !parentClStepsWalker.isComplete()) {
                                    // we update the first not completed step with the proper template (this allows the same template to be used
                                    // multiple times as subchecklist in a single instance. We call it recursively because this template may also
                                    // have a parent...
                                    parentCl.updateStepState(parentClStepsWalker, state.isError() ? EXECUTION_FAILED : OK);
                                    ActorRepository.getPersistenceActor().tell(new PersistenceRequest(parentCl.getId()), null);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public void updateStepsReopenable() {

        for (int i = this.steps.size() - 1; i >= 0; i--) {

            updateStepReopenable(this.steps.get(i));
        }
    }


    public void updateStepReopenable(final Step step) {

        final State state = step.getState();

        boolean reopenable = true;

        setReopenable:
        {
            if (state == null || !state.isReopenable()) {
                reopenable = false;
                break setReopenable;
            }

            if (step.getSubChecklist() != null) {
                reopenable = false;
                break setReopenable;
            }

            for (final Step stepWalker : this.steps) {

                if (stepWalker.dependsOn(step) && (stepWalker.isComplete() && stepWalker.getState() != State.NOT_APPLICABLE)) {
                    reopenable = false;
                    break setReopenable;
                }
            }
        }

        step.setReopenable(reopenable);
    }


    public void close() {

        for (final Step step : this.steps) {

            if (step.getState().isComplete()) {
                step.setReopenable(false);
            } else {
                updateStepState(step, ABORTED);
            }
        }
    }
}
