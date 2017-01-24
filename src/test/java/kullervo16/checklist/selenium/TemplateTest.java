package kullervo16.checklist.selenium;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;

/**
 * End-to-End tests based on the template page.
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
    
    @Test
    public void testInspect() throws Exception {
        getPage("templates.html");
        assertEquals("Log in to Checklist",driver.getTitle());
        
        login("alice", "secret");
        
        assertEquals("Templates",driver.getTitle());
        getDynamicElement("/deployment/firstDeployment_inspect", true).click();
        assertEquals("Checklist",driver.getTitle());
        
        // see that the id is displayed in inspect mode
        assertNotNull(getDynamicElement("_createDeploymentEnvironment", true));
        // check for responsible
        assertEquals("deployer",getDynamicElement("createDeploymentEnvironment_responsible").getText());
        
        // see that we can instantiate the checklist from the inspect screen
        String parent = driver.getWindowHandle();
        getDynamicElement("instantiate").click();
        
        // add a tag to start of
        this.switchToModalDialog(parent);
        getDynamicElement("newTagInput",true).sendKeys("testInspect");
        getDynamicElement("newTagButton").click();
        
        // check that the checklist exists on disk
        String checklistId = getChecklistId();
        assertTrue(new File(TARGETDATA+"/checklists/deployment/firstDeployment/"+checklistId).exists());
        
    }
    
    @Test
    public void testDelete() throws Exception {
        assertTrue(new File(TARGETDATA+"/templates/development/verifyDeployment.yml").exists());
        getPage("templates.html");
        assertEquals("Log in to Checklist",driver.getTitle());
        
        login("alice", "secret");
        
        assertEquals("Templates",driver.getTitle());

        getDynamicElement("/development/verifyDeployment_delete",true).click();
                
        // confirm that we know what we're doing
        Alert alert = driver.switchTo().alert();
        
        assertEquals("Are you sure you want to delete template /development/verifyDeployment? This action cannot be undone...", alert.getText());
        alert.accept();
        
        // damn, we're not allowed to delete this one... ok
        alert = driver.switchTo().alert();
        assertEquals("The template /development/verifyDeployment is referenced by the template /development/verifyDeployment. Please remove the link between those 2 templates before deleting /development/verifyDeployment.",alert.getText());
        alert.dismiss();
        
        
        // so delete another one
        getDynamicElement("/deployment/firstDeployment_delete").click();
        
        alert = driver.switchTo().alert();
        assertEquals("Are you sure you want to delete template /deployment/firstDeployment? This action cannot be undone...", alert.getText());
        alert.accept();
         
        // give some time to update the disk
        Thread.sleep(250);
        
        // verify the second template is gone on disk as well (but the first is still there)
        assertTrue(new File(TARGETDATA+"/templates/development/verifyDeployment.yml").exists());
        assertFalse(new File(TARGETDATA+"/templates/deployment/firstDeployment.yml").exists());
        
        assertNull(getDynamicElement("/deployment/firstDeployment_new"));
    }

}
