
package kullervo16.checklist.model;

import kullervo16.checklist.service.TemplateRepository;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeve
 */
public class TemplateTest {
    
    private TemplateRepository repository;
    
    @Before
    public void setup() {
        this.repository = new TemplateRepository();
        this.repository.loadData("./src/test/resources/data/templates");
    }     
    
    @Test
    public void testDeserialiseTemplate() {
        Template t = this.repository.getTemplate("/deployment/firstDeployment");
        assertNotNull(t);
        List<? extends Step> steps = t.getSteps();
        assertNotNull(steps);
        assertEquals(3,steps.size());
        
        assertEquals("createDeploymentEnvironment", steps.get(0).getId());
        assertEquals("createApplication", steps.get(1).getId());
        assertEquals("verifyFunctioning", steps.get(2).getId());
        
        assertEquals("deployer", steps.get(0).getResponsible());
        assertEquals("deployer", steps.get(1).getResponsible());
        assertEquals("development", steps.get(2).getResponsible());
        
        assertEquals("request the deployment environment", steps.get(0).getAction());
        assertEquals("create the application in the proper zone", steps.get(1).getAction());
        assertNull(steps.get(2).getAction());
        
        assertEquals(1, steps.get(0).getChecks().size());
        assertEquals("log on the deployment station", steps.get(0).getChecks().get(0));
        
        assertEquals(2, steps.get(1).getChecks().size());
        assertEquals("open webconsole in the proper zone", steps.get(1).getChecks().get(0));
        assertEquals("application should be present and green", steps.get(1).getChecks().get(1));
        
        
    }
}
