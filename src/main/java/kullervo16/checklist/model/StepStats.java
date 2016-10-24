
package kullervo16.checklist.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * DTO for step specific statistics.
 *
 * @author jef
 */
public class StepStats {

    private String name;

    private int successRate;

    private int numberOfOccurrences;

    private final List<String> errors = new LinkedList<>();


    public String getName() {
        return name;
    }


    public void setName(final String name) {
        this.name = name;
    }


    public int getSuccessRate() {
        return successRate;
    }


    public void setSuccessRate(final int successRate) {
        this.successRate = successRate;
    }


    public int getNumberOfOccurrences() {
        return numberOfOccurrences;
    }


    public void setNumberOfOccurrences(final int numberOfOccurrences) {
        this.numberOfOccurrences = numberOfOccurrences;
    }


    public List<String> getErrors() {
        return errors;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(name);
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
        final StepStats other = (StepStats) obj;
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        return true;
    }


    public void update(final Step step) {

        errors.addAll(step.getErrors());

        if (step.isComplete()) {
            successRate = ((successRate * numberOfOccurrences) + (step.getErrors().isEmpty() ? 100 : 0)) / (numberOfOccurrences + 1);
            numberOfOccurrences++;
        }
    }
}
