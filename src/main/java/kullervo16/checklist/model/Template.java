/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import kullervo16.checklist.model.persist.TemplatePersister;

/**
 * DTO object for a template. Contains all data, no logic.
 * @author jef
 */
public class Template {
    protected String displayName;
    protected List<Step> steps = new LinkedList<>();
    protected String description;
    protected List<String> tags;    
    protected TemplatePersister persister;
    protected List<Milestone> milestones;

    
    public Template() {
        
    }
    
    public Template(File f) {
        this.persister = new TemplatePersister(f, this);
    }
        
    public String getDisplayName() {
        return displayName;
    }
       

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "TemplateDto{" + "displayName=" + displayName + ", steps=" + steps + '}';
    }
    

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }
    
    public String getDescription() {
        this.checkAndLoadDataFromFile();
        return this.description;
    }

    public List<Milestone> getMilestones() {
        this.checkAndLoadDataFromFile();        
        return this.milestones;
    }

    public List<String> getTags() {
        this.checkAndLoadDataFromFile();
        return this.tags;
    }
    
    public List<? extends Step> getSteps() {
        this.checkAndLoadDataFromFile();
        return this.steps;
    }

    private void checkAndLoadDataFromFile() {
        if(this.persister != null) {
            this.persister.checkAndLoadDataFromFile();
        }
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }
    
    @JsonIgnore
    public TemplatePersister getPersister() {
        return this.persister;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.displayName);
        hash = 31 * hash + Objects.hashCode(this.steps);
        hash = 31 * hash + Objects.hashCode(this.description);
        hash = 31 * hash + Objects.hashCode(this.tags);
        hash = 31 * hash + Objects.hashCode(this.milestones);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Template other = (Template) obj;
        if (!Objects.equals(this.displayName, other.displayName)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.steps, other.steps)) {
            return false;
        }
        if (!Objects.equals(this.tags, other.tags)) {
            return false;
        }
        if (!Objects.equals(this.milestones, other.milestones)) {
            return false;
        }
        return true;
    }
    
    public void persist() {
        this.persister.serialize();
    }
}
