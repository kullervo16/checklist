package kullervo16.checklist.repository;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kullervo16.checklist.messages.PersistenceRequest;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.Template;



/**
 * Repository that parses the directory checklist structure.
 * 
 * @author jeve
 */

public enum ChecklistRepository {
    INSTANCE;
    
    private static Map<String, Checklist> data = new HashMap<>();
    private static File mainFolder;
    private final static Object lock = new Object();
    
    static {
        // TODO : fetch from config
        loadData("/home/jef/NetBeansProjects/checklist/target/test-classes/data/checklists");
    }
    
   /**
     * This method scans the directory structure and determines which templates are available.
     * @param folder the directory to scan
     */
    public static void loadData(String folder) {
        Map<String,Checklist> newData = new HashMap<>();
        synchronized (lock) {
            mainFolder = new File(folder);
            scanDirectoryForTemplates(mainFolder, newData);
            data = newData;
        }
        
    }
    
    private static void scanDirectoryForTemplates(File startDir, Map<String,Checklist> newModel) {
        for(File f : startDir.listFiles()) {
            if(f.isDirectory()) {
                scanDirectoryForTemplates(f, newModel);
            } else {
                newModel.put(f.getName(), new Checklist(f)); 
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
    public String createFromTemplate(String templateId, Template template, String parent) {
        String uuid = UUID.randomUUID().toString();
        File subfolder = new File(mainFolder, templateId);
        synchronized (lock) {
            subfolder.mkdirs();

            Checklist checklist = new Checklist(template,new File(subfolder, uuid), parent);
            data.put(uuid, checklist);
        }
                        
        // TODO : send message to stats to signal updated content        
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(uuid));
        
        return uuid;
    }
}
