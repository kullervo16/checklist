package kullervo16.checklist.model;

import java.io.File;

/**
 * Data object class to model a Checklist... it is backed by a YAML file.
 * 
 * @author jeve
 */
public class Checklist {

    private final File file;

    public Checklist(File file) {
        this.file = file;
    }
    
    
}
