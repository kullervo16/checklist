
package kullervo16.checklist.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Info object for checklists. It's natural ordering is on the last activity.
 *
 * @author jef
 */
public class ChecklistInfo implements Comparable<ChecklistInfo> {

    private final String template;

    private String uuid;

    private final String displayName;

    private final boolean complete;

    private long lastActive;

    private final Set<String> tags;


    public ChecklistInfo(final Checklist cl) {

        template = cl.getTemplate();
        uuid = cl.getId();
        displayName = cl.getDisplayName();
        complete = cl.isComplete();
        lastActive = cl.getCreationTime();
        tags = cl.getTags();

        for (final Step step : cl.getSteps()) {

            if (step.getLastUpdate() != null && step.getLastUpdate().getTime() > lastActive) {
                lastActive = step.getLastUpdate().getTime();
            }
        }
    }


    public void setUuid(final String uuid) {
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


    public Set<String> getTags() {
        return tags;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(uuid);
        return hash;
    }


    @Override
    public boolean equals(final Object obj) {

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

        if (!Objects.equals(uuid, other.uuid)) {
            return false;
        }

        return true;
    }


    /**
     * Sort most recent first.
     */
    @Override
    public int compareTo(final ChecklistInfo t) {

        if (t == null) {
            return 1;
        }

        // cannot use minus, because when the difference is too big (f.e. "now" - 0) the int wraps around
        // and messes up the ordering.
        return Long.valueOf(t.getLastActive()).compareTo(lastActive);
    }

}
