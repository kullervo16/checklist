package kullervo16.checklist.model;

/**
 * Data object class to model a Checklist... 
 * 
 * @author jeve
 */
public interface Checklist extends Template{
      
    
   public boolean isComplete();
   
   /**
    * This method calculates the worst state of the 
    * @return 
    */
   public State getState();
   
   /**
    * This method returns a percentage of progress
    * @return 
    */
   public int getProgress();
   
   /**
    * 
    * @return whether this checklist already has some tags of its own (iso of only the template tags)
    */
   public boolean isSpecificTagSet();

}
