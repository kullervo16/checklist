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
import kullervo16.checklist.model.dto.StepDto;
import kullervo16.checklist.model.dto.TemplateDto;

/**
 * Data object to model a template. It is backed by a YAML file.
 * 
 * @author jeve
 */
public class TemplateImpl extends TemplateDto {

    private File file;
    
    
    // variables for cache control
    private long lastCheck;
    private long fileModifTime;

    public TemplateImpl(File file) {
        this.file = file;
    }

    public TemplateImpl() {
    }
        
    @Override
    public List<? extends StepDto> getSteps() {
        this.checkAndLoadDataFromFile();
        return Collections.unmodifiableList(this.steps);
    }
    
    private void checkAndLoadDataFromFile() {        
        if(this.file != null && System.currentTimeMillis() - lastCheck > 1000 && this.file.lastModified() - this.fileModifTime > 0) {
            YamlReader reader;
            
            try {
                // init
                LinkedList<StepImpl> newStepList = new LinkedList<>();
                this.tags  = new LinkedList<>();
                this.milestones = new LinkedList<>();
                
                reader = new YamlReader(new FileReader(this.file));                
                Map template = (Map) reader.read();
                if(template != null) {
                    for(Map stepMap : (List<Map>)template.get(STEPS)) {
                        newStepList.add(new StepImpl(stepMap));
                    }
                    this.steps = newStepList;
                    this.description = (String) template.get(DESCRIPTION);
                    if(template.get(TAGS) != null) {
                        this.tags.addAll((List<String>) template.get(TAGS));
                    }
                    if(template.get(MILESTONES) != null) {
                        this.milestones.addAll((List<String>) template.get(MILESTONES));     
                    }
                }
                
                this.lastCheck = System.currentTimeMillis();
                this.fileModifTime = file.lastModified();
                this.displayName = this.file.getName();
            } catch (FileNotFoundException | YamlException ex) {
                Logger.getLogger(TemplateImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    

    @Override
    public String getDescription() {
        this.checkAndLoadDataFromFile();
        return this.description;
    }

    @Override
    public List<String> getMilestones() {
        this.checkAndLoadDataFromFile();
        return this.milestones;
    }

    @Override
    public List<String> getTags() {
        this.checkAndLoadDataFromFile();
        return this.tags;
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
            for(Step step : this.steps) {
                if(step instanceof StepImpl) {
                    ((StepImpl)step).serialize(writer);
                }
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
        return "TemplateImpl{" + "steps=" + steps + ", lastCheck=" + lastCheck + ", fileModifTime=" + fileModifTime + '}';
    }   
    
    private static final String STEPS = "steps";
    private static final String DESCRIPTION = "description";
    private static final String MILESTONES = "milestones";
    private static final String TAGS = "tags";
}
