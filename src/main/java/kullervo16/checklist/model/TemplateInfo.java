/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

/**
 * Data object specifying the info for a template
 * @author jef
 */
public class TemplateInfo implements Comparable<TemplateInfo>{
    
    private String name;
    
    private String category;
    
    private String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(TemplateInfo t) {
        if(t == null) {
            return 1;
        }
        return this.getId().compareTo(t.getId());
    }
    
    
}
