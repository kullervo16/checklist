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

}
