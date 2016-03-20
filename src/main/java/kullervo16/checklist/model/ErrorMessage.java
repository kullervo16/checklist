package kullervo16.checklist.model;

/**
 * Class to model an error.
 * @author jef
 */
public class ErrorMessage {
    public enum Severity {WARNING,MINOR,MAJOR, CRITICAL};
    
    private String description;
    
    private Severity severity;
    
    private String details;

    public ErrorMessage(String description, Severity severity, String details) {
        this.description = description;
        this.severity = severity;
        this.details = details;
    }
    
    

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    
    
    
}
