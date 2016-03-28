package kullervo16.checklist.repository;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import kullervo16.checklist.messages.PersistenceRequest;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.ChecklistInfo;
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
        // fixed path... target is to work in a docker container, you can mount it via a volume to whathever you want
        loadData("/opt/checklist/checklists");
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
        if(startDir.listFiles() != null) {
            for(File f : startDir.listFiles()) {
                if(f.isDirectory()) {
                    scanDirectoryForTemplates(f, newModel);
                } else {
                    newModel.put(f.getName(), new Checklist(f)); 
                }
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
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(uuid), null);
        
        return uuid;
    }

    public List<ChecklistInfo> getChecklistInformation() {
        List<ChecklistInfo> result = new LinkedList<>();
        synchronized (lock) {
            for(Entry<String,Checklist> clEntry : data.entrySet()) {
                ChecklistInfo cli = new ChecklistInfo(clEntry.getValue());
                cli.setUuid(clEntry.getKey());
                result.add(cli);
            }
        }
        Collections.sort(result); // sort outside the lock
        return result;
    }

    public Iterable<Checklist> getChecklistsForTemplate(String id) {
        List<Checklist> result = new LinkedList<>();
        synchronized (lock) {
            for(Entry<String,Checklist> clEntry : data.entrySet()) {
                if(id.equals(clEntry.getValue().getTemplate())) {
                    result.add(clEntry.getValue());
                }
            }
        }
        return result;
    }
}

