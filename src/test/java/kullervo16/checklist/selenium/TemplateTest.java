package kullervo16.checklist.selenium;

import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jef
 */
public class TemplateTest extends BaseSeleniumTest {

    @Before
    public void loadData() throws IOException {
        this.instrumentBackend("./src/test/resources/data/templates", null);
    }
    
    @Test
    public void testLoadAdmin() throws Exception {
        getPage("templates.html");
        assertEquals("Log in to Checklist",driver.getTitle());
        
        login("alice", "secret");
        
        assertEquals("Templates",driver.getTitle());
        
        // buttons available for all
        assertNotNull(getDynamicElement("/deployment/firstDeployment_new", true)); // first call... be patient
        assertNotNull(getDynamicElement("/deployment/firstDeployment_inspect")); 
        assertNotNull(getDynamicElement("/deployment/firstDeployment_stats"));

        // buttons available in our role
        assertNotNull(getDynamicElement("/deployment/firstDeployment_delete"));
        assertNotNull(getDynamicElement("/deployment/firstDeployment_upload"));
        assertNotNull(getDynamicElement("/deployment/firstDeployment_download"));
        
        // check the tags
        assertNotNull(getDynamicElement("/deployment/firstDeployment_tag_deployment"));
        assertNotNull(getDynamicElement("/deployment/firstDeployment_tag_software"));
        
         // check the milestones
        assertNotNull(getDynamicElement("/deployment/firstDeployment_ms_readyForDeployment"));
        assertNotNull(getDynamicElement("/deployment/firstDeployment_ms_deployed"));
        
        // check second template
        assertNotNull(getDynamicElement("/development/startProject_new")); 
        assertNotNull(getDynamicElement("/development/startProject_inspect")); 
        assertNotNull(getDynamicElement("/development/startProject_stats"));
        
        // check third template
        assertNotNull(getDynamicElement("/development/verifyDeployment_new")); 
        assertNotNull(getDynamicElement("/development/verifyDeployment_inspect")); 
        assertNotNull(getDynamicElement("/development/verifyDeployment_stats"));
        
        // check description
        assertEquals("Checklist to verify a deployment",getDynamicElement("/development/verifyDeployment_description").getText().trim());
    }
    
    @Test
    public void testLoadModify() throws Exception {
        getPage("templates.html");
        assertEquals("Log in to Checklist",driver.getTitle());
        
        login("marc", "secret");
        
        assertEquals("Templates",driver.getTitle());
        
        // buttons available for all
        assertNotNull(getDynamicElement("/deployment/firstDeployment_new", true)); // first call... be patient
        assertNotNull(getDynamicElement("/deployment/firstDeployment_inspect")); 
        assertNotNull(getDynamicElement("/deployment/firstDeployment_stats"));
        
         // buttons NOT available in our role
        assertNull(getDynamicElement("/deployment/firstDeployment_delete"));
        assertNull(getDynamicElement("/deployment/firstDeployment_upload")); 
        assertNull(getDynamicElement("/deployment/firstDeployment_download"));
        
        // check the tags
        assertNotNull(getDynamicElement("/deployment/firstDeployment_tag_deployment"));
        assertNotNull(getDynamicElement("/deployment/firstDeployment_tag_software"));
        
         // check the milestones
        assertNotNull(getDynamicElement("/deployment/firstDeployment_ms_readyForDeployment"));
        assertNotNull(getDynamicElement("/deployment/firstDeployment_ms_deployed"));
        
        // check second template
        assertNotNull(getDynamicElement("/development/startProject_new")); 
        assertNotNull(getDynamicElement("/development/startProject_inspect")); 
        assertNotNull(getDynamicElement("/development/startProject_stats"));
        
        // check third template
        assertNotNull(getDynamicElement("/development/verifyDeployment_new")); 
        assertNotNull(getDynamicElement("/development/verifyDeployment_inspect")); 
        assertNotNull(getDynamicElement("/development/verifyDeployment_stats"));
        
        // check description
        assertEquals("Checklist to verify a deployment",getDynamicElement("/development/verifyDeployment_description").getText().trim());
    }
}
