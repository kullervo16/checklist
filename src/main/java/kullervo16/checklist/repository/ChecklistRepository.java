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
import kullervo16.checklist.model.Milestone;
import kullervo16.checklist.model.TagcloudEntry;
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
                    Checklist cl = new Checklist(f);
                    cl.setId(f.getName());
                    newModel.put(cl.getId(), cl); 
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

    public String createFromTemplate(String folder, String templateName, Template template, String parent) {
        return this.createFromTemplate("/"+folder+"/"+templateName, template, parent);
    }
    
    /**
     * This methoc creates a new checklist from an existing template.
     * @param template
     * @return a UUID 
     */
    public String createFromTemplate(String templateId, Template template, String parent) {
        Checklist parentCL = null;
        if(parent != null) {
            parentCL = data.get(parent);
            if(parentCL == null) {
                throw new IllegalArgumentException(parent+" is not a known checklist... can't use it as parent");
            }
        }
        String uuid = UUID.randomUUID().toString();
        File subfolder = new File(mainFolder, templateId);
        synchronized (lock) {
            subfolder.mkdirs();

            Checklist checklist = new Checklist(uuid,template,new File(subfolder, uuid), parentCL);
            
            data.put(uuid, checklist);
        }
                        
        // TODO : send message to stats to signal updated content        
        ActorRepository.getPersistenceActor().tell(new PersistenceRequest(uuid), null);
        
        return uuid;
    }

    public List<ChecklistInfo> getChecklistInformation(String tag, String milestoneName) {
        List<ChecklistInfo> result = new LinkedList<>();
        Milestone milestone = null;
        if(milestoneName != null) {
            milestone = new Milestone(milestoneName, true);
        }
        synchronized (lock) {
            for(Entry<String,Checklist> clEntry : data.entrySet()) {
                if(tag != null) {
                    if(!clEntry.getValue().getTags().contains(tag)) {
                        continue;
                    }
                }
                if(milestone != null) {
                    if(!clEntry.getValue().getMilestones().contains(milestone)) {
                        continue;
                    }
                }
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

    public List<TagcloudEntry> getTagInfo() {
        Map<String,Integer> tagMap = new HashMap<>();
        synchronized (lock) {
            for(Checklist cl : data.values()) {
                for(String tag : cl.getTags()) {
                    if(tagMap.containsKey(tag)) {
                        tagMap.put(tag, tagMap.get(tag)+1);
                    } else {
                        tagMap.put(tag,1);
                    }
                }
            }
        }
        List<TagcloudEntry> result = new LinkedList<>();
        for(Entry<String,Integer> tmEntry : tagMap.entrySet()) {
            result.add(new TagcloudEntry(tmEntry.getKey(), tmEntry.getValue()));
        }
        return result;
    }

    public List<TagcloudEntry> getMilestoneInfo() {
        Map<String,Integer> msMap = new HashMap<>();
        synchronized (lock) {
            for(Checklist cl : data.values()) {
                for(Milestone ms : cl.getMilestones()) {
                    if(!ms.isReached()) {
                        continue;
                    }
                    if(msMap.containsKey(ms.getName())) {
                        msMap.put(ms.getName(), msMap.get(ms.getName())+1);
                    } else {
                        msMap.put(ms.getName(),1);
                    }
                }
            }
        }
        List<TagcloudEntry> result = new LinkedList<>();
        for(Entry<String,Integer> tmEntry : msMap.entrySet()) {
            result.add(new TagcloudEntry(tmEntry.getKey(), tmEntry.getValue()));
        }
        return result;
    }

    public void deleteChecklist(Checklist cl) {
        data.remove(cl.getId());
        if(cl.getPersister().getFile() != null) {
            cl.getPersister().getFile().delete();
        }
    }
}

