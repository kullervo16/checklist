package kullervo16.checklist.service;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.Template;
import kullervo16.checklist.model.dto.ChecklistDto;



/**
 * Repository that parses the directory checklist structure.
 * 
 * @author jeve
 */
@Singleton
public class ChecklistRepository {
    private Map<String, Checklist> data = new HashMap<>();
    private File mainFolder;
    
    
    @PostConstruct
    public void init() {
        // TODO : fetch from config
        this.loadData("/home/jef/NetBeansProjects/checklist/target/test-classes/data/checklists");
    }
    
   /**
     * This method scans the directory structure and determines which templates are available.
     * @param folder the directory to scan
     */
    public synchronized void loadData(String folder) {
        Map<String,Checklist> newData = new HashMap<>();
        this.mainFolder = new File(folder);
        this.scanDirectoryForTemplates(this.mainFolder, newData);
        this.data = newData;
        
    }
    
    private void scanDirectoryForTemplates(File startDir, Map<String,Checklist> newModel) {
        for(File f : startDir.listFiles()) {
            if(f.isDirectory()) {
                this.scanDirectoryForTemplates(f, newModel);
            } else {
                newModel.put(f.getName(), new ChecklistDto(f)); 
            }
            
        }
    }
    
    

    public List<String> getChecklistNames() {
        LinkedList<String> names = new LinkedList<>(data.keySet());        
        return names;
                
    }

    public Checklist getChecklist(String name) {
        return this.data.get(name);
    }

    /**
     * This methoc creates a new checklist from an existing template.
     * @param template
     * @return a UUID 
     */
    public synchronized  String createFromTemplate(String templateId, Template template) {
        String uuid = UUID.randomUUID().toString();
        File subfolder = new File(this.mainFolder, templateId);
        subfolder.mkdirs();
        
        ChecklistDto checklist = new ChecklistDto(template,new File(subfolder, uuid));
        this.data.put(uuid, checklist);
        
        // TODO : send message to stats to signal updated content
        // TODO : send message to persistance to sync with storage backend
        return uuid;
    }
}
