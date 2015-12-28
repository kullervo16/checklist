package kullervo16.checklist.model;

import java.util.List;

/**
 * Interface description of a template. 
 * 
 * @author jeve
 */
public interface Template {

    
    /**
     * 
     * @return a list of steps that form the basis of the checklist this template describes.
     */
    public List<? extends Step> getSteps();

    /**
     * 
     * @return the name to display when showing this template
     */
    public String getDisplayName();
        
    /**
     * 
     * @return human readable information of the procedure this checklist supports
     */
    public String getDescription();

    /**
     * 
     * @return a list of milestones in the procedure... may be empty
     */
    public List<Milestone> getMilestones();

    /**
     * 
     * @return a set of common tags all checklist based on this template will share... should be related to the procedure.
     */
    public List<String> getTags();
    
}
