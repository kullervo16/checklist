package kullervo16.checklist.model;

import java.util.LinkedList;
import java.util.List;



/**
 * Repository that parses the directory template structure.
 * 
 * @author jeve
 */
public class TemplateRepository {

    public List<String> getTemplateNames() {
        LinkedList<String> names = new LinkedList<>();
        names.add("boe");
        names.add("baa");
        return names;
                
    }
}
