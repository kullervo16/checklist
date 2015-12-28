/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

import java.util.Objects;

/**
 * Class to model a milestone that is reached when the checks are ok up to a given point.
 * 
 * @author jef
 */
public class Milestone {
    
    private String name;
    
    private boolean reached;

    public Milestone(String name, boolean reached) {
        this.name = name;
        this.reached = reached;
    }

    public Milestone() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReached() {
        return reached;
    }

    public void setReached(boolean reached) {
        this.reached = reached;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + (this.reached ? 1 : 0);
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
        final Milestone other = (Milestone) obj;
        if (this.reached != other.reached) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Milestone{" + "name=" + name + ", reached=" + reached + '}';
    }
    
    
}
