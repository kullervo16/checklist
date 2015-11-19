package kullervo16.checklist.model;

import java.util.LinkedList;
import java.util.List;



/**
 * Repository that parses the directory checklist structure.
 * 
 * @author jeve
 */
public class ChecklistRepository {

    public List<String> getChecklistNames() {
        LinkedList<String> names = new LinkedList<>();
        names.add("boe");
        names.add("baa");
        return names;
                
    }
}
