package kullervo16.checklist.model.persist;

import kullervo16.checklist.model.*;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import kullervo16.checklist.model.Step;

/**
 * Data object to model a template. It is backed by a YAML file.
 * 
 * @author jeve
 */
public class TemplatePersister  {

    private final File file;
    protected Template template;   
    private boolean writing;
    private boolean loaded;
    
    
    // variables for cache control
    private long lastCheck;
    private long fileModifTime;

    public TemplatePersister(File file,Template t) {
        this.file = file;
        this.template = t;
    }    
        
    
    
    public void checkAndLoadDataFromFile() {        
        if(!this.writing && this.file != null && System.currentTimeMillis() - lastCheck > 1000 && this.file.lastModified() - this.fileModifTime > 0) {
            YamlReader reader;
            
            try {
                // init
                LinkedList<Step> steps = new LinkedList<>();
                LinkedList<String> tags  = new LinkedList<>();
                LinkedList<Milestone> milestones = new LinkedList<>();    
                                
                
                reader = new YamlReader(new FileReader(this.file));                
                Map templateMap = (Map) reader.read();
                if(templateMap != null) {
                    handleData(templateMap, steps, milestones, tags);                    
                    
                }
                this.template.setTags(tags);
                this.template.setMilestones(milestones);
                this.template.setSteps(steps);
                
                this.lastCheck = System.currentTimeMillis();
                this.fileModifTime = file.lastModified();     
                
                
                
            } catch (FileNotFoundException | YamlException ex) {
                Logger.getLogger(TemplatePersister.class.getName()).log(Level.SEVERE, null, ex);
                this.loaded = false;
                return;
            }            
        }
        this.loaded = true;
    }

    protected void handleData(Map templateMap, LinkedList<Step> steps, LinkedList<Milestone> milestones, LinkedList<String> tags) {
        for(Map stepMap : (List<Map>)templateMap.get(STEPS)) {
            Step step = new Step(stepMap, steps);
            steps.add(step);
            if(step.getMilestone() != null) {
                milestones.add(step.getMilestone());
            }
        }
        
        this.template.setDescription((String) templateMap.get(DESCRIPTION));
        if(templateMap.get(TAGS) != null) {
            tags.addAll((List<String>) templateMap.get(TAGS));
        }
        if(templateMap.get("displayName") != null) {
            this.template.setDisplayName((String) templateMap.get("displayName"));
        }
    }
    
    /**
     * This method makes sure the template adheres to the proper structure
     * @param content
     */
    public static List<ErrorMessage> validateTemplate(String content) {
        LinkedList<ErrorMessage> result = new LinkedList<>();
        try {
            YamlReader reader = new YamlReader(content);            
            Map templateMap = null;
            try {
                templateMap = (Map) reader.read();
            }catch(ClassCastException cce ) {
                result.add(new ErrorMessage("Invalid YAML.",ErrorMessage.Severity.CRITICAL,"No YAML structure found."));
            }
            if(templateMap != null) {
                
            }
        }catch(YamlException e) {
            result.add(new ErrorMessage("Invalid YAML.",ErrorMessage.Severity.CRITICAL,e.getMessage()));
        }
        return result;
    }
                
    private void serializeStep(Step step, PrintWriter writer) {        
        printLine(writer,"- id",step.getId());
        printLine(writer,"  responsible",step.getResponsible());
        printLine(writer,"  action",step.getAction());
        printLine(writer,"  state",step.getState().toString());
        printLine(writer,"  executor",step.getExecutor());        
        printLine(writer,"  comment",step.getComment());
        printLine(writer,"  documentation",step.getDocumentation());
        printLine(writer,"  subchecklist", step.getSubChecklist());
        printLine(writer,"  weight", ""+step.getWeight());
        printLine(writer,"  selectedOption", step.getSelectedOption());
        if(step.getLastUpdate() != null) {
            printLine(writer,"  lastUpdate",""+step.getLastUpdate().getTime());
        }
        if(step.getChecks().size() == 1) {
            printLine(writer,"  check",step.getChecks().get(0));
        } else if (step.getChecks().size() > 1) {
            writer.append("    ").append("check: \n");            
            for(String check : step.getChecks()) {
                writer.append("      -").append(" step: ").append(check).append("\n");
            }
        }
        if(step.getMilestone() != null) {
            writer.append("    ").append("milestone: \n");    
            writer.append("    ").append("  - ").append("name: ").append(step.getMilestone().getName()).append("\n");    
            writer.append("    ").append("  - ").append("reached: ").append(""+step.getMilestone().isReached()).append("\n");    
        }
        
    }
    
    private void printLine(PrintWriter writer, String name, String value) {
        if(value != null) {
            writer.append("  ").append(name).append(": ").append(value.replaceAll("\n","##NEWLINE##")).append("\n");
        }
    }
            
    /**
     * Method can be overwritten by subclasses to present the header.
     * @param writer 
     */
    protected void serializeHeader(PrintWriter writer) {
        if(template.getDisplayName() != null) {
            writer.append("displayName: ").append(this.template.getDisplayName()).append("\n");
        }
        if(template.getDescription() != null) {
            writer.append("description: ").append(this.template.getDescription()).append("\n");
        }
        
        if(!template.getTags().isEmpty()) {
            writer.append("tags: ").append("\n");
            for(String tag : template.getTags()) {
                writer.append("    - ").append(tag).append("\n");
            }
        }        
    }
    
    public void serialize() {  
        if(!this.loaded) {
            // do this to prevent an unloaded version to be erased !
            this.checkAndLoadDataFromFile();
        }
        
        this.writing = true;
        try(PrintWriter writer = new PrintWriter(this.file);) {
            this.serializeHeader(writer);
            writer.append("steps:\n");
            for(Step step : this.template.getSteps()) {
                if(step instanceof Step) {
                    this.serializeStep((Step)step, writer);
                }
            }
            writer.flush();        
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
        this.lastCheck = System.currentTimeMillis();
        this.fileModifTime = this.file.lastModified();
        this.writing = false;
    }

    @Override
    public int hashCode() {
        this.checkAndLoadDataFromFile();
        int hash = 7;
        hash = 89 * hash + this.template.hashCode();
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
        final TemplatePersister other = (TemplatePersister) obj;
        if (!Objects.equals(this.template, other.template)) {
            return false;
        }
        return true;
    }

    
    
    
    private static final String STEPS = "steps";
    private static final String DESCRIPTION = "description";
    private static final String MILESTONES = "milestones";
    private static final String TAGS = "tags";       

    public File getFile() {
        return this.file;
    }
}
