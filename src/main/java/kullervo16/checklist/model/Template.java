package kullervo16.checklist.model;

import java.util.List;

/**
 * Data object to model a template. 
 * 
 * @author jeve
 */
public interface Template {

    
    
    public List<? extends Step> getSteps();

    public String getDisplayName();
        
    
    
}
