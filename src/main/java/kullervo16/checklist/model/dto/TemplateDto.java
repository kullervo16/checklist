/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model.dto;

import java.util.LinkedList;
import java.util.List;
import kullervo16.checklist.model.Template;

/**
 * DTO object for a template. Contains all data, no logic.
 * @author jef
 */
public class TemplateDto implements Template{
    protected String displayName;
    protected List<? extends StepDto> steps = new LinkedList<>();
    protected String description;
    protected List<String> tags;
    protected List<String> milestones;                

    @Override
    public List<? extends StepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<? extends StepDto> steps) {
        this.steps = steps;
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

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public List<String> getMilestones() {
        return this.milestones;
    }

    @Override
    public List<String> getTags() {
        return this.tags;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setMilestones(List<String> milestones) {
        this.milestones = milestones;
    }
    
    
}
