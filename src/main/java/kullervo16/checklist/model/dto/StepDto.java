package kullervo16.checklist.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kullervo16.checklist.model.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data object to model a step in a template/checklist.
 * @author jeve
 */
public class StepDto implements Step {

    protected String id;
    protected String responsible;
    protected String action;
    protected List<String> checks;    
    protected State state;
    protected String executor;
    protected Date lastUpdate;
    protected String comment;
    protected Milestone milestone;

    public StepDto(Map stepMap) {        
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
        
        if(stepMap.containsKey("milestone")) {
            Milestone ms;
            if(stepMap.get("milestone") instanceof String) {
                ms = new Milestone((String) stepMap.get("milestone"), false);
            } else {
                Map<String,String> mileStoneMap = (Map) stepMap.get("milestone");
                
                ms = new Milestone(mileStoneMap.get("name"), mileStoneMap.get("reached").equals("true"));
            }
            this.milestone = ms;
        }
    }

    public StepDto() {
    }
    
    

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setChecks(List<String> checks) {
        this.checks = checks;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
   
    @Override
    public String getResponsible() {
        return responsible;
    }

    @Override
    public String getAction() {
        return action;
    }


    @Override
    public List<String> getChecks() {
        return checks;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public String getExecutor() {
        return executor;
    }

    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }
    
    
    /**
     *
     * @param state
     */
    @Override
    public void setState(State state) {
        this.state = state;
        this.lastUpdate = new Date();
    }

    @Override
    public void setExecutor(String executor) {
        this.executor = executor;
    }

    @Override
    @JsonIgnore
    public boolean isComplete() {
        switch(this.state) {
            case UNKNOWN:
            case ON_HOLD:
                return false;
            default:
                return true;
        }
    }

    public void setComment(String text) {
        this.comment = text;
    }

    public String getComment() {
        return this.comment;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.id);
        hash = 67 * hash + Objects.hashCode(this.responsible);
        hash = 67 * hash + Objects.hashCode(this.action);
        hash = 67 * hash + Objects.hashCode(this.checks);
        hash = 67 * hash + Objects.hashCode(this.state);
        hash = 67 * hash + Objects.hashCode(this.executor);
        hash = 67 * hash + Objects.hashCode(this.lastUpdate);
        hash = 67 * hash + Objects.hashCode(this.comment);
        hash = 67 * hash + Objects.hashCode(this.milestone);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StepDto other = (StepDto) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.responsible, other.responsible)) {
            return false;
        }
        if (!Objects.equals(this.action, other.action)) {
            return false;
        }
        if (!Objects.equals(this.executor, other.executor)) {
            return false;
        }
        if (!Objects.equals(this.comment, other.comment)) {
            return false;
        }
        if (!Objects.equals(this.milestone, other.milestone)) {
            return false;
        }
        if (!Objects.equals(this.checks, other.checks)) {
            return false;
        }
        if (this.state != other.state) {
            return false;
        }
        if (!Objects.equals(this.lastUpdate, other.lastUpdate)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StepDto{" + "id=" + id + ", responsible=" + responsible + ", action=" + action + ", checks=" + checks + ", state=" + state + ", executor=" + executor + ", lastUpdate=" + lastUpdate + ", comment=" + comment + ", milestone=" + milestone + '}';
    }

    
}
