package kullervo16.checklist.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;
import java.util.LinkedList;
import kullervo16.checklist.model.*;
import java.util.List;
import kullervo16.checklist.model.persist.ChecklistPersister;

/**
 * Data object class to model a Checklist... it is backed by a YAML file.
 * 
 * @author jeve
 */
public class ChecklistDto extends TemplateDto implements Checklist{
      
    
    public ChecklistDto() {
    }
    
    public ChecklistDto(File file) {
        this.persister = new ChecklistPersister(file, this);
    }

    public ChecklistDto(Template template, File file) {
        this(file);
        this.description = template.getDescription();
        this.displayName = template.getDisplayName();
        this.tags = template.getTags();
        // deep copies... we're working completely in memory here, don't want to link references...
        this.milestones = new LinkedList<>();
        for(Milestone ms : template.getMilestones()) {
            this.milestones.add(new Milestone(ms.getName(), ms.isReached()));
        }        
        this.steps = new LinkedList<>();
        for(StepDto step : (List<StepDto>) template.getSteps()) {
            this.steps.add(new StepDto(step));
        }
        
        
    }
        
    
    
   @Override
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
   @Override
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
   @Override
   @JsonIgnore
   public int getProgress() {
       List<? extends Step> stepWalker = this.getSteps();
       int totalSteps = stepWalker.size();
       int stepsToDo = 0;
       for(Step step : stepWalker) {
           if(!step.isComplete()) {
               stepsToDo ++;
           }
       }
       return (int)((totalSteps - stepsToDo) / (totalSteps * 0.01));
   }

    

}
