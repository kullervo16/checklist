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
public class Checklist extends Template{
      
    private boolean hasSpecificTags;
    private String parent;
    private String template;
    
    public Checklist() {
    }
    
    public Checklist(File file) {
        this.persister = new ChecklistPersister(file, this);
    }

    public Checklist(Template template, File file, String parent) {
        this(file);
        this.description = template.getDescription();
        this.displayName = template.getDisplayName();
        this.template    = template.getId();
        
        // deep copy... we're working completely in memory here, don't want to link references...  
        this.tags = new LinkedList<>(template.getTags());
        this.milestones = new LinkedList<>();
        this.steps = new LinkedList<>();
        for(Step step : (List<Step>) template.getSteps()) {
            Step newStep = new Step(step, this.steps);
            this.steps.add(newStep);
            if(newStep.getMilestone() != null) {
                // recreate the milestone list from the steps (needs to point to the same instance to allow proper update when the step is updated)
                this.milestones.add(newStep.getMilestone());
            }
        }        
        if(parent != null) {
            this.tags.add("subchecklist");
            this.hasSpecificTags = true;
            this.parent = parent;
        } else {
            this.hasSpecificTags = false;
        }
        
        
    }
        
    
       
   @JsonIgnore
   public boolean isComplete() {
       for(Step step : this.getSteps()) {
           if(!step.isComplete()) {
               // there is at least 1 not yet executed step in the list
               return false;
           }
       }
       return true;
   } 
   
   /**
    * This method calculates the worst state of the 
    * @return 
    */   
   @JsonIgnore
   public State getState() {       
       State aggregatedState = State.UNKNOWN;
       for(Step step : this.getSteps()) {
           if(step.getState().compareTo(aggregatedState) > 0) {
               aggregatedState = step.getState();
           }
       }
       return aggregatedState;
   }
   
   /**
    * This method returns a percentage of progress
    * @return 
    */   
   public int getProgress() {
       List<? extends Step> stepWalker = this.getSteps();
       int totalSteps = 0;
       int stepsToDo = 0;
       for(Step step : stepWalker) {
           if(!step.isComplete()) {
               stepsToDo += step.getWeight();
           }
           totalSteps += step.getWeight();
       }
       if(stepsToDo == 0) {
           return 100; // prevent rounding issues (complete should be 100%, not 99.999 :-) )
       }
       return (int)((totalSteps - stepsToDo) / (totalSteps * 0.01));
   }

    public void setProgress(int progress) {
        // ignore, progress is calculated, but otherwise JAX-RS complains (however, we want it in the JSON for our angular client).
    }
    
    public boolean isSpecificTagSet() {
        return this.hasSpecificTags;
    }

    public void setSpecificTagSet(boolean hasSpecificTags) {
        this.hasSpecificTags = hasSpecificTags;
    }
    
    public String getParent() {
        this.checkAndLoadDataFromFile();
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }            

    public String getTemplate() {
        this.checkAndLoadDataFromFile();
        return this.template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
        
}
