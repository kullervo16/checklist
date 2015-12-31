/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import kullervo16.checklist.model.Milestone;
import kullervo16.checklist.model.Step;
import kullervo16.checklist.model.Template;
import kullervo16.checklist.model.persist.TemplatePersister;

/**
 * DTO object for a template. Contains all data, no logic.
 * @author jef
 */
public class TemplateDto implements Template{
    protected String displayName;
    protected List<StepDto> steps = new LinkedList<>();
    protected String description;
    protected List<String> tags;    
    protected TemplatePersister persister;
    protected List<Milestone> milestones;

    
    public TemplateDto() {
        
    }
    
    public TemplateDto(File f) {
        this.persister = new TemplatePersister(f, this);
    }
        
    @Override
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
    
    @Override
    public String getDescription() {
        this.checkAndLoadDataFromFile();
        return this.description;
    }

    @Override
    public List<Milestone> getMilestones() {
        this.checkAndLoadDataFromFile();        
        return this.milestones;
    }

    @Override
    public List<String> getTags() {
        this.checkAndLoadDataFromFile();
        return this.tags;
    }
    
    @Override
    public List<? extends Step> getSteps() {
        this.checkAndLoadDataFromFile();
        return this.steps;
    }

    private void checkAndLoadDataFromFile() {
        if(this.persister != null) {
            this.persister.checkAndLoadDataFromFile();
        }
    }

    public void setSteps(List<StepDto> steps) {
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
        final TemplateDto other = (TemplateDto) obj;
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
    
        
}
