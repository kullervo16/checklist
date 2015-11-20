/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeve
 */
public class ChecklistRepositoryTest {
    
    private ChecklistRepository repository;
    
    @Before
    public void setup() {
        this.repository = new ChecklistRepository();
        this.repository.loadData("./src/test/resources/data/checklists");
    }

    /**
     * Test of getChecklistNames method, of class ChecklistRepository.
     */
    @Test
    public void testGetChecklistNames() {
        System.out.println("getChecklistNames");
        
        List<String> result = this.repository.getChecklistNames();
        assertEquals(1, result.size());
        
        assertTrue(result.contains("/vas/famhp/acc/deployment_20151124_1.yml"));
    }
    
    @Test
    public void testGetChecklist() {
        Checklist cl = this.repository.getChecklist("/vas/famhp/acc/deployment_20151124_1.yml");
        assertNotNull(cl);
    }
    
}
