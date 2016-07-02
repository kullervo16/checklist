package kullervo16.checklist.model.persist;

import kullervo16.checklist.model.*;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.File;
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
            
            try (FileReader fileReader =  new FileReader(this.file)){
                // init
                LinkedList<Step> steps = new LinkedList<>();
                LinkedList<String> tags  = new LinkedList<>();
                LinkedList<Milestone> milestones = new LinkedList<>();    
                                
                
                reader = new YamlReader(fileReader);                
                Map templateMap = (Map) reader.read();
                if(templateMap != null) {
                    handleData(templateMap, steps, milestones, tags);                    
                    
                }
                this.template.setTags(tags);
                this.template.setMilestones(milestones);
                this.template.setSteps(steps);
                
                this.lastCheck = System.currentTimeMillis();
                this.fileModifTime = file.lastModified();     
                
                
                
            } catch (IOException ex) {
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
        if(templateMap.get(DISPLAY_NAME) != null) {
            this.template.setDisplayName((String) templateMap.get(DISPLAY_NAME));
        }
        if(templateMap.get(SUBCHECKLIST_ONLY) != null) {
            this.template.setSubchecklistOnly("true".equalsIgnoreCase((String)templateMap.get(SUBCHECKLIST_ONLY)));
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
                // step 1 : check presence of main level tags
                checkTag(templateMap, DISPLAY_NAME, "/", result, DataType.STRING, false);
                checkTag(templateMap, DESCRIPTION, "/", result,DataType.STRING, true);
                checkTag(templateMap, TAGS, "/", result,DataType.LIST, true);
                checkTag(templateMap, STEPS, "/", result,DataType.LIST, true);
                
                // step 2 : check tags
                if(templateMap.containsKey(TAGS)) {
                    try {
                        List<String> currentTagList = new LinkedList<>();
                        for(String tag : (List<String>) templateMap.get(TAGS)) {
                            if(currentTagList.contains(tag)) {
                                result.add(new ErrorMessage("Duplicate tag : "+tag, ErrorMessage.Severity.WARNING, "You used the same tag twice in the template"));
                            } else {
                                currentTagList.add(tag);
                            }
                                    
                        }
                    }catch(ClassCastException cce) {
                        result.add(new ErrorMessage("Wrong data in Tags",ErrorMessage.Severity.MAJOR,"/tags : value should be simple strings"));
                    }
                }
                
                // step 3 : check steps
                if(templateMap.containsKey(STEPS)) {
                    List<Map> stepList = (List<Map>) templateMap.get(STEPS);
                    int pos = 1;
                    List<String> currentIdList = new LinkedList<>();
                    for(Map stepMap : stepList) {
                        checkStep(stepMap, pos++, result);
                        if(stepMap.containsKey("id")) {
                            if(currentIdList.contains(stepMap.get("id"))) {
                                result.add(new ErrorMessage("Duplicate id : "+stepMap.get("id"), ErrorMessage.Severity.MAJOR, "You used the same id twice in the template"));
                            } else {
                                currentIdList.add(stepMap.get("id").toString());
                            }
                        }
                    }
                }
            }
        }catch(YamlException e) {
            result.add(new ErrorMessage("Invalid YAML.",ErrorMessage.Severity.CRITICAL,e.getMessage()));
        }
        return result;
    }
    
    private enum DataType  {STRING, URL, NUMBER, POSITIVE_NUMBER, MAP, LIST, STRING_OR_LIST};
    
    private static void checkTag(Map data, String tagName, String path,List<ErrorMessage> errorList, DataType dataType, boolean mandatory) {
        if(!data.containsKey(tagName)) {
            if(mandatory) {
                errorList.add(new ErrorMessage("Missing tag : "+tagName, ErrorMessage.Severity.MAJOR, path+"/"+tagName));
            }
        } else {
            switch(dataType) {
                case STRING:
                    try {
                        String value = (String) data.get(tagName);
                        value.length();
                    }catch(ClassCastException cce) {
                        errorList.add(new ErrorMessage("Invalid tag value: "+tagName, ErrorMessage.Severity.MAJOR, path+"/"+tagName+" should be a valid String"));
                    }
                    break;
                case MAP:
                    try {
                        Map value = (Map) data.get(tagName);
                        value.size();
                    }catch(ClassCastException cce) {
                        errorList.add(new ErrorMessage("Invalid tag value: "+tagName, ErrorMessage.Severity.MAJOR, path+"/"+tagName+" should be a valid Map"));
                    }
                    break;
                case LIST:
                    try {
                        List value = (List) data.get(tagName);
                        value.size();
                    }catch(ClassCastException cce) {
                        errorList.add(new ErrorMessage("Invalid tag value: "+tagName, ErrorMessage.Severity.MAJOR, path+"/"+tagName+" should be a valid List"));
                    }
                    break;
                case STRING_OR_LIST:
                    try {
                        String value = (String) data.get(tagName);
                        value.length();
                    }catch(ClassCastException cce) {
                        try {
                            // second try : maybe it's a list
                            List value = (List) data.get(tagName);
                            value.size();
                        } catch(ClassCastException cce2) {
                            errorList.add(new ErrorMessage("Invalid tag value: "+tagName, ErrorMessage.Severity.MAJOR, path+"/"+tagName+" should be a valid String or List"));
                        }
                    }
                    break;
                case NUMBER:
                    try {
                        Integer value = Integer.valueOf(data.get(tagName).toString());                        
                    }catch(NumberFormatException cce) {
                        errorList.add(new ErrorMessage("Invalid tag value: "+tagName, ErrorMessage.Severity.MAJOR, path+"/"+tagName+" should be a valid Integer"));
                    }
                    break;
                case POSITIVE_NUMBER:
                    try {
                        Integer value = Integer.valueOf(data.get(tagName).toString());    
                        if(value<0) {
                            errorList.add(new ErrorMessage("Invalid tag value: "+tagName, ErrorMessage.Severity.MAJOR, path+"/"+tagName+" should be a valid positive Integer"));
                        }
                    }catch(NumberFormatException cce) {
                        errorList.add(new ErrorMessage("Invalid tag value: "+tagName, ErrorMessage.Severity.MAJOR, path+"/"+tagName+" should be a valid Integer"));
                    }
                    break;
            }
        }
        
    }
        
    private static void checkStep(Map stepMap, int pos, LinkedList<ErrorMessage> result) {
        // step 1 : all mandatory/optional tags and their types
        checkTag(stepMap, "id", "/steps/"+pos, result, DataType.STRING, true);
        checkTag(stepMap, "responsible", "/steps/"+pos, result,DataType.STRING, true);
        checkTag(stepMap, "options", "/steps/"+pos, result,DataType.LIST, false);
        checkTag(stepMap, "milestone", "/steps/"+pos, result,DataType.STRING, false);
        checkTag(stepMap, "condition", "/steps/"+pos, result,DataType.LIST, false);
        checkTag(stepMap, "action", "/steps/"+pos, result,DataType.STRING, false);
        checkTag(stepMap, "documentation", "/steps/"+pos, result,DataType.URL, false);
        checkTag(stepMap, "subchecklist", "/steps/"+pos, result,DataType.STRING, false);
        checkTag(stepMap, "check", "/steps/"+pos, result,DataType.STRING_OR_LIST, false);
        checkTag(stepMap, "weight", "/steps/"+pos, result,DataType.POSITIVE_NUMBER, false);
        // step 2 : either subchecklist, question or action must be present
        int count = 0;
        if(stepMap.containsKey("subchecklist")) count++;
        if(stepMap.containsKey("action")) count++;
        if(stepMap.containsKey("question")) count++;
        
        if(count == 0) {
            result.add(new ErrorMessage("Subchecklist, question or action is mandatory", ErrorMessage.Severity.MAJOR, "/steps/"+pos+" contains neither subchecklist, question nor action. One of the 2 is required."));
        } else if(count != 1) {
            result.add(new ErrorMessage("More than 1 of (subchecklist, question, action) specified", ErrorMessage.Severity.MAJOR, "/steps/"+pos+" contains more than 1 of (subchecklist, question,action). Only one is allowed.")); 
        }
    }
                
    protected void serializeStep(Step step, PrintWriter writer) {        
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
        printLine(writer,"  question", step.getQuestion());
        printLine(writer,"  answerType", step.getAnswerType());
        if(step.getLastUpdate() != null) {
            printLine(writer,"  lastUpdate",""+step.getLastUpdate().getTime());
        }
        if(step.getChecks().size() == 1) {
            printLine(writer,"  check",step.getChecks().get(0));
        } else if (step.getChecks().size() > 1) {
            writer.append(SEPARATOR_1).append("check: \n");            
            step.getChecks().stream().forEach((check) -> {
                writer.append("      -").append(" step: ").append(check).append("\n");
            });
        }
        if(step.getMilestone() != null) {
            writer.append(SEPARATOR_1).append("milestone: \n");    
            writer.append(SEPARATOR_1).append(SEPARATOR_2).append("name: ").append(step.getMilestone().getName()).append("\n");    
            writer.append(SEPARATOR_1).append(SEPARATOR_2).append("reached: ").append(""+step.getMilestone().isReached()).append("\n");    
        }
        if(step.getOptions() != null) {
            writer.append(SEPARATOR_1).append("options:\n");
            step.getOptions().stream().forEach((option) -> {
                writer.append(SEPARATOR_1).append(SEPARATOR_2).append(option).append("\n");
            });
        }
        
    }
    protected static final String SEPARATOR_2 = "  - ";
    protected static final String SEPARATOR_1 = "    ";
    
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
            template.getTags().stream().forEach((tag) -> {
                writer.append("    - ").append(tag).append("\n");
            });
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
    private static final String TAGS = "tags";       
    private static final String DISPLAY_NAME = "displayName";
    private static final String SUBCHECKLIST_ONLY = "subchecklistOnly";

    public File getFile() {
        return this.file;
    }
}
