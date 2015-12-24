package kullervo16.checklist.model.impl;

import kullervo16.checklist.model.*;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data object to model a template. It is backed by a YAML file.
 * 
 * @author jeve
 */
public class TemplateImpl implements Template {

    private File file;
    
    private List<StepImpl> steps = new LinkedList<>();
    private String displayName;
    
    // variables for cache control
    private long lastCheck;
    private long fileModifTime;

    public TemplateImpl(File file) {
        this.file = file;
    }

    public TemplateImpl() {
    }
        
    
    private void checkAndLoadDataFromFile() {        
        if(this.file != null && System.currentTimeMillis() - lastCheck > 1000 && this.file.lastModified() - this.fileModifTime > 0) {
            YamlReader reader;
            LinkedList<StepImpl> newStepList = new LinkedList<>();
            try {
                reader = new YamlReader(new FileReader(this.file));
                Map template = (Map) reader.read();
                for(Map stepMap : (List<Map>)template.get("steps")) {
                    newStepList.add(new StepImpl(stepMap));
                }
                this.steps = newStepList;
                this.lastCheck = System.currentTimeMillis();
                this.fileModifTime = file.lastModified();
                this.displayName = this.file.getName();
            } catch (FileNotFoundException | YamlException ex) {
                Logger.getLogger(TemplateImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    public List<? extends Step> getSteps() {
        this.checkAndLoadDataFromFile();        
        return Collections.unmodifiableList(this.steps);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    
    
    /**
     * Method can be overwritten by subclasses to present the header.
     * @param writer 
     */
    protected void serializeHeader(PrintWriter writer) {
        ; // do nothing
    }
    
    public void serialize() {  
        // do this to prevent an unloaded version to be erased !
        this.checkAndLoadDataFromFile();
        try(PrintWriter writer = new PrintWriter(this.file);) {
            writer.append("steps:\n");
            for(StepImpl step : this.steps) {
                step.serialize(writer);
            }
            writer.flush();        
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public int hashCode() {
        this.checkAndLoadDataFromFile();
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.steps);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        this.checkAndLoadDataFromFile();
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TemplateImpl other = (TemplateImpl) obj;
        if (!Objects.equals(this.steps, other.steps)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Template{" + "steps=" + steps + ", lastCheck=" + lastCheck + ", fileModifTime=" + fileModifTime + '}';
    }
    
    
}
