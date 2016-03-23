
package kullervo16.checklist.model;

import java.util.List;
import java.util.Objects;

/**
 * Info object for checklists. It's natural ordering is on the last activity.
 * 
 * @author jef
 */
public class ChecklistInfo implements Comparable<ChecklistInfo>{

    private final String template;
    
    private String uuid;
    
    private final String displayName;
    
    private final boolean complete;
    
    private long lastActive;
    
    private final List<String> tags;

    public ChecklistInfo(Checklist cl) {
        this.template = cl.getTemplate();
        this.uuid = cl.getId();
        this.displayName = cl.getDisplayName();
        this.complete = cl.isComplete();        
        for(Step step : cl.getSteps()) {
            if(step.getLastUpdate() != null && step.getLastUpdate().getTime() > lastActive) {
                lastActive = step.getLastUpdate().getTime();
            }
        }
        this.tags = cl.getTags();
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    
    public String getTemplate() {
        return template;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isComplete() {
        return complete;
    }

    public long getLastActive() {
        return lastActive;
    }

    public List<String> getTags() {
        return tags;
    }
    
    
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.uuid);
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
        final ChecklistInfo other = (ChecklistInfo) obj;
        if (!Objects.equals(this.uuid, other.uuid)) {
            return false;
        }
        return true;
    }

    @Override
    /**
     * Sort most recent first.
     */
    public int compareTo(ChecklistInfo t) {
        if(t == null) {            
            return 1;
        }
        // cannot use minus, because when the difference is too big (f.e. "now" - 0) the int wraps around
        // and messes up the ordering.
        return Long.valueOf(t.getLastActive()).compareTo(Long.valueOf(this.lastActive));
    }
    
    
}
