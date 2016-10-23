
package kullervo16.checklist.model;

import java.util.LinkedList;
import java.util.List;

/**
 * DTO for template statistics.
 *
 * @author jef
 */
public class TemplateStats {

    private String id;

    private int numberOfOccurrences;

    private final List<StepStats> currentStepList = new LinkedList<>();

    private final List<StepStats> otherStepList = new LinkedList<>();


    public String getId() {
        return id;
    }


    public void setId(final String id) {
        this.id = id;
    }


    public int getNumberOfOccurrences() {
        return numberOfOccurrences;
    }


    public void setNumberOfOccurrences(final int numberOfOccurrences) {
        this.numberOfOccurrences = numberOfOccurrences;
    }


    public List<StepStats> getCurrentStepList() {
        return currentStepList;
    }


    public List<StepStats> getOtherStepList() {
        return otherStepList;
    }
}
