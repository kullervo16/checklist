package kullervo16.checklist.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Data object to model a step in a template/checklist.
 * @author jeve
 */
public class Step {

    private String id;
    private String responsible;
    private String action;
    private List<String> checks;

    Step(Map stepMap) {
        this.id = (String) stepMap.get("id");
        this.responsible = (String) stepMap.get("responsible");
        this.action = (String) stepMap.get("action");
        this.checks = new LinkedList<>();
        if(stepMap.get("check") instanceof String) {
           // convert String to list (this makes it a lot easier to configure the yaml           
           this.checks.add((String) stepMap.get("check"));
        } else {
            for(Map<String,String> entry : (List<Map>) stepMap.get("check")) {
                this.checks.add(entry.get("step"));
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<String> getChecks() {
        return checks;
    }

    public void setChecks(List<String> checks) {
        this.checks = checks;
    }
    
    
}
