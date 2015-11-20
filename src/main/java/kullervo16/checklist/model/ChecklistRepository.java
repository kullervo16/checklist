package kullervo16.checklist.model;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



/**
 * Repository that parses the directory checklist structure.
 * 
 * @author jeve
 */
public class ChecklistRepository {
    private Map<String, Checklist> data = new HashMap<>();

    
    
   /**
     * This method scans the directory structure and determines which templates are available.
     * @param folder the directory to scan
     */
    public void loadData(String folder) {
        Map<String,Checklist> newData = new HashMap<>();
        this.scanDirectoryForTemplates(new File(folder), "", newData);
        this.data = newData;
    }
    
    private void scanDirectoryForTemplates(File startDir, String prefix,Map<String,Checklist> newModel) {
        for(File f : startDir.listFiles()) {
            if(f.isDirectory()) {
                this.scanDirectoryForTemplates(f, prefix+"/"+f.getName(), newModel);
            } else {
                newModel.put(prefix+"/"+f.getName(), new Checklist(f)); 
            }
            
        }
    }
    
    

    public List<String> getChecklistNames() {
        LinkedList<String> names = new LinkedList<>(data.keySet());        
        return names;
                
    }
}
