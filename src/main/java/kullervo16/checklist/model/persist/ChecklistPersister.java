package kullervo16.checklist.model.persist;

import java.io.File;
import kullervo16.checklist.model.dto.ChecklistDto;

/**
 * Data object class to persist a Checklist... it is backed by a YAML file and adds more data to the template persister.
 * 
 * @author jeve
 */
public class ChecklistPersister extends TemplatePersister {

    public ChecklistPersister(File file, ChecklistDto cl) {
        super(file, cl);
    }
      
    
    
    
    
    
    
   

}
