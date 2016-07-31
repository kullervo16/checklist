package kullervo16.checklist.model.persist;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Map;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.Milestone;
import kullervo16.checklist.model.Step;

/**
 * Data object class to persist a Checklist... it is backed by a YAML file and adds more data to the template persister.
 * 
 * @author jeve
 */
public class ChecklistPersister extends TemplatePersister {

    public ChecklistPersister(File file, Checklist cl) {
        super(file, cl);
    }

    @Override
    protected void serializeHeader(PrintWriter writer) {
        super.serializeHeader(writer); 
        Checklist cl = (Checklist)template;
        if(cl.getParent() != null) {
            writer.append("parent: ").append(cl.getParent()).append("\n");
        }
        writer.append("template: ").append(cl.getTemplate()).append("\n");
        writer.append("specificTagSet: ").append(""+cl.isSpecificTagSet()).append("\n");
        writer.append("uniqueTagcombination: ").append(""+cl.isUniqueTagcombination()).append("\n");
    }

    @Override
    protected void handleData(Map templateMap, LinkedList<Step> steps, LinkedList<Milestone> milestones, LinkedList<String> tags) {
        super.handleData(templateMap, steps, milestones, tags); 
        Checklist cl = (Checklist)template;
        if(templateMap.containsKey("template")) {
            cl.setTemplate((String) templateMap.get("template"));
        }
        if(templateMap.containsKey("specificTagSet")) {
            cl.setSpecificTagSet(((String) templateMap.get("specificTagSet")).equals("true"));
        }    
        if(templateMap.containsKey("parent")) {
            cl.setParent((String)templateMap.get("parent"));
        }
        if(templateMap.containsKey("uniqueTagcombination")) {
            cl.setUniqueTagcombination("true".equalsIgnoreCase((String)templateMap.get("uniqueTagcombination")));
        } else {
            // backwards compatibility : if not set, say true...
            cl.setUniqueTagcombination(true);
        }
    }

    @Override
    protected void serializeStep(Step step, PrintWriter writer) {
        super.serializeStep(step, writer); 
        if(!step.getErrors().isEmpty()) {
            writer.append(SEPARATOR_1).append("errors:\n");
            step.getErrors().stream().forEach((error) -> {
                writer.append(SEPARATOR_1).append(SEPARATOR_2).append(error).append("\n");
            });
        }
        if(!step.getAnswers().isEmpty()) {
            writer.append(SEPARATOR_1).append("answers:\n");
            step.getAnswers().stream().forEach((answer) -> {
                writer.append(SEPARATOR_1).append(SEPARATOR_2).append(answer).append("\n");
            });
        }
    }

    
      

}
