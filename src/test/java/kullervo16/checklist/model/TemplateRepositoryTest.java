/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeve
 */
public class TemplateRepositoryTest {
    
    private TemplateRepository repository;
    
    @Before
    public void setup() {
        this.repository = new TemplateRepository();
        this.repository.loadData("./src/test/resources/data/templates");
    }
        /**
     * Test of getTemplateNames method, of class TemplateRepository.
     */
    @Test
    public void testGetTemplateNames() {
        System.out.println("getTemplateNames");
                
        List<String> result = this.repository.getTemplateNames();
        assertEquals(2, result.size());
        assertTrue(result.contains("/deployment/firstDeployment.yml"));
        assertTrue(result.contains("/development/startProject.yml"));
    }
    
}
