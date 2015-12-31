package kullervo16.checklist.model.persist;

import java.io.File;
import java.io.PrintWriter;
import kullervo16.checklist.model.Checklist;

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
    }
      
    
    
    
    
    
    
   

}
