package kullervo16.checklist.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kullervo16.checklist.model.*;
import java.util.List;

/**
 * Data object class to model a Checklist... it is backed by a YAML file.
 * 
 * @author jeve
 */
public class ChecklistDto extends TemplateDto implements Checklist{
      
    

    public ChecklistDto() {
    }
        
    
    
   @Override
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
