package kullervo16.checklist.model;

import java.util.Date;
import java.util.List;

/**
 * Data object to model a step in a template/checklist.
 * @author jeve
 */
public interface  Step {

   

    public String getId();
   
    public String getResponsible();

    public String getAction();


    public List<String> getChecks();

    public State getState();

    public String getExecutor();

    public Date getLastUpdate();


    public void setState(State state);

    public void setExecutor(String executor);

    public boolean isComplete();

    public void setComment(String text);

    public String getComment();
   
    public List<String> getErrors();
    
    public Milestone getMilestone();
    
    public int getWeight();
    
    public String getDocumentation();
    
    public String getSubChecklist();
}
