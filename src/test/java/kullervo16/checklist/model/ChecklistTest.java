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
public class ChecklistTest {
    
    private ChecklistRepository repository;
    
    @Before
    public void setup() {
        this.repository = new ChecklistRepository();
        this.repository.loadData("./src/test/resources/data/checklists");
    }
    
    @Test
    public void testNonExisting() {
        Checklist cl = this.repository.getChecklist("/vas/famhp/acc/non_existing.yml");
        assertNull(cl);
    }
    
    @Test
    public void testDeserialise() {
        Checklist cl = this.repository.getChecklist("/vas/famhp/acc/deployment_20151124.yml");
        assertNotNull(cl);
        List<Step> steps = cl.getSteps();
        assertNotNull(steps);
        assertEquals(3, steps.size());
        
        assertEquals("createSecureGit", steps.get(0).getId());
        assertEquals("createApplication", steps.get(1).getId());
        assertEquals("odtInit", steps.get(2).getId());
        
        assertEquals("middleware", steps.get(0).getResponsible());
        assertEquals("middleware", steps.get(1).getResponsible());
        assertEquals("middleware", steps.get(2).getResponsible());
        
        assertEquals("request secure GIT to JDSS. Request URL", steps.get(0).getAction());
        assertEquals("create the application in the proper zone", steps.get(1).getAction());
        assertEquals("perform odt init", steps.get(2).getAction());
        
        assertEquals(1, steps.get(0).getChecks().size());
        assertEquals("on the deployment station, perform git clone with the URL from JDSS", steps.get(0).getChecks().get(0));
        
        assertEquals(2, steps.get(1).getChecks().size());
        assertEquals("verify proper gear type (must match environment)", steps.get(1).getChecks().get(0));
        assertEquals("rhc ssh <application> -n <domain>", steps.get(1).getChecks().get(1));
        
        assertEquals(State.OK, steps.get(0).getState());
        assertEquals(State.OK, steps.get(1).getState());
        assertEquals(State.NOK, steps.get(2).getState());
        
        assertEquals("ikke", steps.get(0).getExecutor());
        assertEquals("gij", steps.get(1).getExecutor());
        assertEquals("hij", steps.get(2).getExecutor());
    }
    
    @Test
    public void testGetState() {
        Checklist cl = this.repository.getChecklist("/vas/famhp/acc/deployment_20151124.yml");
        assertEquals(State.NOK, cl.getState());
    }
    
    @Test
    public void testIsComplete() {
        Checklist cl = this.repository.getChecklist("/vas/famhp/acc/deployment_20151124.yml");
        assertTrue(cl.isComplete());
        Checklist cl2 = this.repository.getChecklist("/vas/famhp/acc/deployment_20151125.yml");
        assertFalse(cl2.isComplete());
    }
    
    @Test
    public void testGetProgress() {
        Checklist cl = this.repository.getChecklist("/vas/famhp/acc/deployment_20151124.yml");
        assertEquals(100, cl.getProgress());
        Checklist cl2 = this.repository.getChecklist("/vas/famhp/acc/deployment_20151125.yml");
        assertEquals(66, cl2.getProgress());
    }
}
