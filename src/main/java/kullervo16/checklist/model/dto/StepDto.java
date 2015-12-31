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
    protected List<String> errors;
    protected int weight;
    protected String documentation;
    protected String subChecklist;
    protected List<String> options;
    protected String selectionOption;
    protected Condition condition;
    
    public StepDto() {
    }

    /**
     * copy constructor.
     * @param step 
     */
    StepDto(StepDto step, List<StepDto> stepList) {
        this.action = step.getAction();
        this.checks = new LinkedList<>(step.getChecks());
        this.comment= step.getComment();
        this.executor=step.getExecutor();
        this.id     = step.getId();
        this.lastUpdate = step.getLastUpdate();
        this.milestone  = step.getMilestone() == null ? null : new Milestone(step.getMilestone().getName(),step.getMilestone().isReached());
        this.responsible= step.getResponsible();
        this.state  = step.getState();
        this.errors = new LinkedList<>(step.getErrors());
        this.weight = step.getWeight();
        this.documentation = step.getDocumentation();
        this.subChecklist = step.getSubChecklist();
        this.options = step.getOptions();
        this.selectionOption = step.getSelectedOption();
        if(step.getCondition() != null) {
           StepDto selectionPoint = null;
           for(StepDto walker : stepList) {
               if(walker.equals(step.getCondition().getStep())) {
                   selectionPoint = walker;
               }
           }
           this.condition = new Condition(selectionPoint, step.getCondition().getSelectedOption());
        }
    }

    /**
     * Yaml constructor
     * @param stepMap 
     * @param stepList list with the other steps, needed for wiring
     */
    public StepDto(Map stepMap, List<StepDto> stepList) {        
        this.id = (String) stepMap.get("id");
        for(StepDto walker : stepList) {
            if(walker.getId().equals(this.id)) {
                throw new IllegalStateException("Duplicate step id "+this.id);
            }
        }
        
        this.responsible = (String) stepMap.get("responsible");
        this.action = (String) stepMap.get("action");
        this.checks = new LinkedList<>();
        this.executor = (String)stepMap.get("executor");
        this.documentation = (String) stepMap.get("documentation");
        this.subChecklist = (String) stepMap.get("subchecklist");
                
        this.weight   = 1;                
        if(stepMap.get("weight") != null) {
            this.weight = Integer.parseInt(stepMap.get("weight").toString());
        }        
        
        if(stepMap.get("check") != null) {
            if(stepMap.get("check") instanceof String) {
               // convert String to list (this makes it a lot easier to configure the yaml           
               this.checks.add((String) stepMap.get("check"));
            } else {
                for(Map<String,String> entry : (List<Map>) stepMap.get("check")) {
                    this.checks.add(entry.get("step"));
                }
            }
        }
        this.errors = new LinkedList<>();
        if(stepMap.get("errors") != null) {
            if(stepMap.get("errors") instanceof String) {
               // convert String to list (this makes it a lot easier to configure the yaml           
               this.errors.add((String) stepMap.get("errors"));
            } else {
                for(String error : (List<String>) stepMap.get("errors")) {
                    this.checks.add(error);
                }
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
        
        if(stepMap.containsKey("options")) {
            this.options = new LinkedList<>((List<String>)stepMap.get("options"));            
        }        
        
        if(stepMap.containsKey("condition")) {
            StepDto selectionPoint = null;
            for(StepDto walker : stepList) {
                if(walker.getId().equals(((List<Map>)stepMap.get("condition")).get(0).get("selectionPoint"))) {
                    selectionPoint = walker;
                }
            }
            if(selectionPoint == null) {
                throw new IllegalStateException("Unable to meet condition for step "+this.id);
            }
            this.condition = new Condition(selectionPoint, (String) ((List<Map>)stepMap.get("condition")).get(1).get("option"));
        }
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
            case EXECUTED:
            case CHECK_FAILED_NO_COMMENT:
            case EXECUTION_FAILED_NO_COMMENT:
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
        hash = 97 * hash + Objects.hashCode(this.id);
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
        return true;
    }

    

    @Override
    public String toString() {
        return "StepDto{" + "id=" + id + ", responsible=" + responsible + ", action=" + action + ", checks=" + checks + ", state=" + state + ", executor=" + executor + ", lastUpdate=" + lastUpdate + ", comment=" + comment + ", milestone=" + milestone + '}';
    }

    @Override
    public List<String> getErrors() {
        return this.errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String getDocumentation() {
        return this.documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @Override
    public String getSubChecklist() {
        return this.subChecklist;
    }

    public void setSubChecklist(String subChecklist) {
        this.subChecklist = subChecklist;
    }

    @Override
    public List<String> getOptions() {
        return this.options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getSelectedOption() {
        return this.selectionOption;
    }

    public void setSelectionOption(String selectionOption) {
        this.selectionOption = selectionOption;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }
    
    
    
}
