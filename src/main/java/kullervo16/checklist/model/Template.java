/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import kullervo16.checklist.model.persist.TemplatePersister;

/**
 * DTO object for a template. Contains all data, no logic.
 *
 * @author jef
 */
public class Template {

    protected String displayName;

    protected List<Step> steps = new LinkedList<>();

    protected String description;

    protected Set<String> tags = new HashSet<>();

    protected TemplatePersister persister;

    protected List<Milestone> milestones = new LinkedList<>();

    protected String id;

    protected boolean subchecklistOnly;

    protected long creationTime;
    
    protected String user;


    public Template() {

    }


    public Template(final File f) {
        persister = new TemplatePersister(f, this);
    }


    public String getDisplayName() {
        checkAndLoadDataFromFile();
        return displayName;
    }


    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }


    @Override
    public String toString() {
        return "TemplateDto{" + "displayName=" + displayName + ", steps=" + steps + '}';
    }


    public void setDescription(final String description) {
        this.description = description;
    }


    public void setTags(final Collection<String> tags) {
        this.tags = new HashSet<>(tags);
    }


    public void setMilestones(final List<Milestone> milestones) {
        this.milestones = milestones;
    }


    public String getDescription() {
        checkAndLoadDataFromFile();
        return description;
    }


    public List<Milestone> getMilestones() {
        checkAndLoadDataFromFile();
        return milestones;
    }


    public Set<String> getTags() {
        checkAndLoadDataFromFile();
        return Collections.unmodifiableSet(tags);
    }


    public List<? extends Step> getSteps() {
        checkAndLoadDataFromFile();
        return steps;
    }


    public Step getStepById(final String id) {

        for (final Step step : steps) {

            if (step.getId().equals(id)) {
                return step;
            }
        }

        return null;
    }


    protected void checkAndLoadDataFromFile() {
        if (persister != null) {
            persister.checkAndLoadDataFromFile();
        }
    }


    public void setSteps(final List<Step> steps) {
        this.steps = steps;
    }


    @JsonIgnore
    public TemplatePersister getPersister() {
        return persister;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(displayName);
        hash = 31 * hash + Objects.hashCode(steps);
        hash = 31 * hash + Objects.hashCode(description);
        hash = 31 * hash + Objects.hashCode(tags);
        hash = 31 * hash + Objects.hashCode(milestones);
        return hash;
    }


    @Override
    public boolean equals(final Object obj) {
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
        if (!Objects.equals(displayName, other.displayName)) {
            return false;
        }
        if (!Objects.equals(description, other.description)) {
            return false;
        }
        if (!Objects.equals(steps, other.steps)) {
            return false;
        }
        if (!Objects.equals(tags, other.tags)) {
            return false;
        }
        if (!Objects.equals(milestones, other.milestones)) {
            return false;
        }
        return true;
    }


    public void persist(boolean force) {
        persister.serialize(force);
    }


    public String getId() {
        checkAndLoadDataFromFile();
        return id;
    }


    public void setId(final String id) {
        this.id = id;
    }


    public boolean isSubchecklistOnly() {
        return subchecklistOnly;
    }


    public void setSubchecklistOnly(final boolean subchecklistOnly) {
        this.subchecklistOnly = subchecklistOnly;
    }


    public long getCreationTime() {
        return creationTime;
    }


    public void setCreationTime(final long creationTime) {
        this.creationTime = creationTime;
    }
    
    public void setUser(String userName) {
        this.user = userName;
    }

    public String getUser() {
        return user;
    }
}
