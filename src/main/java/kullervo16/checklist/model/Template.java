package kullervo16.checklist.model;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data object to model a template. It is backed by a YAML file.
 * 
 * @author jeve
 */
public class Template {

    private final File file;
    
    private List<Step> steps;
    
    // variables for cache control
    private long lastCheck;
    private long fileModifTime;

    public Template(File file) {
        this.file = file;
    }
    
    private void checkAndLoadDataFromFile() {
        if(System.currentTimeMillis() - lastCheck > 1000 && this.file.lastModified() - this.fileModifTime > 0) {
            YamlReader reader;
            LinkedList<Step> newStepList = new LinkedList<>();
            try {
                reader = new YamlReader(new FileReader(this.file));
                Map template = (Map) reader.read();
                for(Map stepMap : (List<Map>)template.get("steps")) {
                    newStepList.add(new Step(stepMap));
                }
                this.steps = newStepList;
                this.lastCheck = System.currentTimeMillis();
                this.fileModifTime = file.lastModified();
            } catch (FileNotFoundException | YamlException ex) {
                Logger.getLogger(Template.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    public List<Step> getSteps() {
        this.checkAndLoadDataFromFile();
        return this.steps;
    }

    public String getDisplayName() {
        return this.file.getName();
    }
}
