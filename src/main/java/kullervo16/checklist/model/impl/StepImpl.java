package kullervo16.checklist.model.impl;

import kullervo16.checklist.model.*;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data object to model a step in a template/checklist.
 * @author jeve
 */
public class StepImpl implements Step {

    private final String id;
    private final String responsible;
    private final String action;
    private final List<String> checks;    
    private State state;
    private String executor;
    private Date lastUpdate;
    private String comment;

    public StepImpl(Map stepMap) {        
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
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.id);
        hash = 29 * hash + Objects.hashCode(this.responsible);
        hash = 29 * hash + Objects.hashCode(this.action);
        hash = 29 * hash + Objects.hashCode(this.checks);
        hash = 29 * hash + Objects.hashCode(this.state);
        hash = 29 * hash + Objects.hashCode(this.executor);
        hash = 29 * hash + Objects.hashCode(this.lastUpdate);
        hash = 29 * hash + Objects.hashCode(this.comment);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StepImpl other = (StepImpl) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.responsible, other.responsible)) {
            return false;
        }
        if (!Objects.equals(this.action, other.action)) {
            return false;
        }
        if (!Objects.equals(this.checks, other.checks)) {
            return false;
        }
        if (this.state != other.state) {
            return false;
        }
        if (!Objects.equals(this.executor, other.executor)) {
            return false;
        }
        if (!Objects.equals(this.lastUpdate, other.lastUpdate)) {
            return false;
        }
        if (!Objects.equals(this.comment, other.comment)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Step{" + "id=" + id + ", responsible=" + responsible + ", action=" + action + ", checks=" + checks + ", state=" + state + ", executor=" + executor + ", lastUpdate=" + lastUpdate + ", comment=" + comment + '}';
    }

    void serialize(PrintWriter writer) {        
        printLine(writer,"- id",this.id);
        printLine(writer,"  responsible",this.responsible);
        printLine(writer,"  action",this.action);
        printLine(writer,"  state",this.state.toString());
        printLine(writer,"  executor",this.executor);        
        printLine(writer,"  comment",this.comment);
        if(this.lastUpdate != null) {
            printLine(writer,"  lastUpdate",""+this.lastUpdate.getTime());
        }
        if(this.checks.size() == 1) {
            printLine(writer,"  check",this.checks.get(0));
        } else if (this.checks.size() > 1) {
            writer.append("    ").append("check").append(": ").append("\n");            
            for(String check : this.checks) {
                writer.append("      -").append(" step").append(": ").append(check).append("\n");
            }
        }
        
    }

    private void printLine(PrintWriter writer, String name, String value) {
        if(value != null) {
            writer.append("  ").append(name).append(": ").append(value.replaceAll("\n","##NEWLINE##")).append("\n");
        }
    }
    
    
}
