package kullervo16.checklist.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Data object to model a step in a template/checklist.
 * @author jeve
 */
public class Step {

    private final String id;
    private final String responsible;
    private final String action;
    private final List<String> checks;    
    private State state;
    private String executor;
    private Date lastUpdate;

    public Step(Map stepMap) {        
        this.id = (String) stepMap.get("id");
        this.responsible = (String) stepMap.get("responsible");
        this.action = (String) stepMap.get("action");
        this.checks = new LinkedList<>();
        this.executor = (String)stepMap.get("executor");
        
        if(stepMap.get("check") instanceof String) {
           // convert String to list (this makes it a lot easier to configure the yaml           
           this.checks.add((String) stepMap.get("check"));
        } else {
            for(Map<String,String> entry : (List<Map>) stepMap.get("check")) {
                this.checks.add(entry.get("step"));
            }
        }
        
        if(stepMap.containsKey("state")) {
            this.state = State.valueOf((String)stepMap.get("state"));
        } else {
            this.state = State.UNKNOWN;        
        }
    }

    public String getId() {
        return id;
    }
   
    public String getResponsible() {
        return responsible;
    }

    public String getAction() {
        return action;
    }


    public List<String> getChecks() {
        return checks;
    }

    public State getState() {
        return state;
    }

    public String getExecutor() {
        return executor;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }


    public void setState(State state) {
        this.state = state;
        this.lastUpdate = new Date();
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    
    
}
