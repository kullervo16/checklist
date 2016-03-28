
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

    public void setName(String name) {
        this.name = name;
    }

    public int getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(int successRate) {
        this.successRate = successRate;
    }

    public int getNumberOfOccurrences() {
        return numberOfOccurrences;
    }

    public void setNumberOfOccurrences(int numberOfOccurrences) {
        this.numberOfOccurrences = numberOfOccurrences;
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.name);
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
        final StepStats other = (StepStats) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    
    public void update(Step step) {
        
        this.errors.addAll(step.getErrors());
        if(step.isComplete()) {
            this.successRate = (int)(((this.successRate * this.numberOfOccurrences) + (step.getErrors().isEmpty() ? 100 : 0)) / (this.numberOfOccurrences+1));        
            this.numberOfOccurrences++;
        }
    }
    
    
}
