
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
        Template t = this.repository.getTemplate("/deployment/firstDeployment.yml");
        assertNotNull(t);
        List<Step> steps = t.getSteps();
        assertNotNull(steps);
        assertEquals(3,steps.size());
        
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
        
        
    }
}
