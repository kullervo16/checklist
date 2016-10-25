package kullervo16.checklist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import kullervo16.checklist.model.persist.ChecklistPersister;

/**
 * Data object class to model a Checklist... it is backed by a YAML file.
 *
 * @author jeve
 */
public class Checklist extends Template {

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
        tags = new LinkedList<>(template.getTags());
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

            tags.add("subchecklist");

            for (final String parentTag : parent.getTags()) {
                if (!tags.contains(parentTag)) {
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

        final List<? extends Step> stepWalker = getSteps();
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


    public boolean isSubchecklist() {
        return parent != null;
    }
}
