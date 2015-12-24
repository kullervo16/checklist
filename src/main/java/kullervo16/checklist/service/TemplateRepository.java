package kullervo16.checklist.service;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import kullervo16.checklist.model.Template;
import kullervo16.checklist.model.dto.TemplateDto;
import kullervo16.checklist.model.impl.TemplateImpl;



/**
 * Repository that parses the directory template structure.
 * 
 * @author jeve
 */
@Singleton
public class TemplateRepository {

    private Map<String,TemplateImpl> data = new HashMap<>();
    
    @PostConstruct
    public void init() {
        // TODO : fetch from config
        this.loadData("/home/jef/NetBeansProjects/checklist/src/test/resources/data/templates");
    }
        
    /**
     * This method scans the directory structure and determines which templates are available.
     * @param folder the directory to scan
     */
    
    public void loadData(String folder) {
        Map<String,TemplateImpl> newData = new HashMap<>();
        this.scanDirectoryForTemplates(new File(folder), "", newData);
        this.data = newData;
    }
    
    private void scanDirectoryForTemplates(File startDir, String prefix,Map<String,TemplateImpl> newModel) {
        for(File f : startDir.listFiles()) {
            if(f.isDirectory()) {
                this.scanDirectoryForTemplates(f, prefix+"/"+f.getName(), newModel);
            } else {
                newModel.put(prefix+"/"+f.getName().substring(0,f.getName().lastIndexOf(".")), new TemplateImpl(f)); 
            }
            
        }
    }

    public List<String> getTemplateNames() {
        LinkedList<String> names = new LinkedList<>(data.keySet());        
        return names;
                
    }

    public TemplateDto getTemplate(String templateName) {
        return this.data.get(templateName);
    }

    
}
