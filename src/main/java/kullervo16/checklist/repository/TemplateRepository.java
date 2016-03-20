package kullervo16.checklist.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import kullervo16.checklist.model.ErrorMessage;
import kullervo16.checklist.model.Template;
import kullervo16.checklist.model.TemplateInfo;
import kullervo16.checklist.model.persist.TemplatePersister;
import org.apache.commons.io.IOUtils;



/**
 * Repository that parses the directory template structure.
 * 
 * @author jeve
 */

public enum TemplateRepository {
    INSTANCE;
    
    // create a ref here to make sure the actors get initialized
    static ActorRepository actors = new ActorRepository();

    private static Map<String,Template> data = new HashMap<>();
    private static final String TEMPLATE_DIR = "/opt/checklist/templates";
    
    static {
        // fixed path ... target is to work in a docker container, you can mount it via a volume to whathever you want
        loadData(TEMPLATE_DIR);        
    }
        
    /**
     * This method scans the directory structure and determines which templates are available.
     * @param folder the directory to scan
     */
    
    public static void loadData(String folder) {
        Map<String,Template> newData = new HashMap<>();
        scanDirectoryForTemplates(new File(folder), "", newData);
        data = newData;
    }
    
    private static void scanDirectoryForTemplates(File startDir, String prefix,Map<String,Template> newModel) {
        if(startDir.listFiles() != null) {
            for(File f : startDir.listFiles()) {
                if(f.isDirectory()) {
                    scanDirectoryForTemplates(f, prefix+"/"+f.getName(), newModel);
                } else {
                    Template template = new Template(f);
                    String id = prefix+"/"+f.getName().substring(0,f.getName().lastIndexOf("."));
                    template.setDisplayName(id);
                    template.setId(id);
                    newModel.put(id, template); 
                }

            }
        }
    }

    public List<String> getTemplateNames() {
        LinkedList<String> names = new LinkedList<>(data.keySet());        
        return names;
                
    }

    public Template getTemplate(String templateName) {
        return data.get(templateName);
    }

    public List<TemplateInfo> getTemplateInformation() {
        List<TemplateInfo> result = new LinkedList<>();
        Map<String,Template> snapshot = Collections.unmodifiableMap(data);
        for(Entry<String,Template> entry : snapshot.entrySet()) {
            TemplateInfo ti = new TemplateInfo();
            ti.setId(entry.getKey());
            
            ti.setDescription(entry.getValue().getDescription());    
            ti.setMilestones(entry.getValue().getMilestones());
            ti.setTags(entry.getValue().getTags());
            result.add(ti);
        }
        Collections.sort(result);
        return result;
    }

    public List<ErrorMessage> validateAndUpdate(String name, InputStream inputStream) throws IOException {
        String content = IOUtils.toString(inputStream);
        
        List<ErrorMessage> errors = TemplatePersister.validateTemplate(content);
        
        for(ErrorMessage err : errors) {
            if(!err.getSeverity().equals(ErrorMessage.Severity.WARNING)) {
                // warning is the highest severity we allow and still update
                return errors;
            }
        }
        
        File targetFile;
        if(data.containsKey(name)) {
            // update of an existing template
            targetFile = data.get(name).getPersister().getFile();
            
        } else {
            // new file
            targetFile = new File(TEMPLATE_DIR+"/"+name);
            
        }
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                IOUtils.write(content, fos);                
                data.put(name, new Template(targetFile));
            }catch(IOException ioe) {
                errors.add(new ErrorMessage("Cannot write template to file", ErrorMessage.Severity.CRITICAL, ioe.getMessage()));
            }
        
        return errors;
    }

    
}
