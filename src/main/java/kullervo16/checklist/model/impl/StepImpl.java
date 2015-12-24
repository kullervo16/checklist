package kullervo16.checklist.model.impl;

import java.io.PrintWriter;
import java.util.Map;
import kullervo16.checklist.model.dto.StepDto;

/**
 * Data object to model a step in a template/checklist.
 * @author jeve
 */
public class StepImpl extends StepDto {

    public StepImpl(Map stepMap) {
        super(stepMap);
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
