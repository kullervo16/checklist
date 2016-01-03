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
    }

    
      

}
