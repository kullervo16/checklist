package kullervo16.checklist.selenium;

import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * End-to-End tests based on the checklist page.
 * 
 * @author jef
 */
public class ChecklistTest extends BaseSeleniumTest {

    @Before
    public void loadData() throws IOException {
        this.instrumentBackend("./src/test/resources/data/templates", null);
    }
    
    @Test
    public void testCreateWithTags() throws Exception {
        getPage("templates.html");
        assertEquals("Log in to Checklist",driver.getTitle());
        
        login("alice", "secret");
        
        assertEquals("Templates",driver.getTitle());
        
        // buttons available for all
        assertNotNull(getDynamicElement("/deployment/firstDeployment_new", true)); // first call... be patient
        getDynamicElement("/deployment/firstDeployment_new", false).click();
        
        
        getDynamicElement("newTagInput", true).sendKeys("testTag");
        
        getDynamicElement("newTagButton").click();
        Thread.sleep(1000); //need some time to reload
        
        
        assertTrue("creation user should be present as a tag", getDynamicElement("tagList", true).getText().contains("@alice")); 
        assertTrue("template category should be present as a tag", getDynamicElement("tagList").getText().contains("deployment")); 
        assertTrue("template should be present as a tag", getDynamicElement("tagList").getText().contains("firstDeployment")); 
        assertTrue("our newly specified tag should be present",getDynamicElement("tagList").getText().contains("testTag"));
        
        getDynamicElement("addTagButton").click();
        getDynamicElement("newTagInput", true).sendKeys("nextTag");
        getDynamicElement("newTagButton").click();
        Thread.sleep(1000); //need some time to reload
        String tags = getDynamicElement("tagList", true).getText();
        System.out.println(tags);
        assertTrue("our newly specified tag should be present",getDynamicElement("tagList").getText().contains("nextTag"));
        
        
        getDynamicElement("addTagButton").click();
        getDynamicElement("newTagInput", true).sendKeys("testTag");
        
        getDynamicElement("newTagButton").click();
        Thread.sleep(1000); //need some time to reload
        assertEquals("Duplicate tag should be ignored", getDynamicElement("tagList", true).getText(), tags);
    }
    
    // testcase for #119 : test that we can add all kinds of chars for a tag
    @Test
    public void testCreateWithTagWithSpecialChars() throws Exception {
        getPage("templates.html");
        assertEquals("Log in to Checklist",driver.getTitle());
        
        login("alice", "secret");
        
        assertEquals("Templates",driver.getTitle());
        
        // buttons available for all
        assertNotNull(getDynamicElement("/deployment/firstDeployment_new", true)); // first call... be patient
        getDynamicElement("/deployment/firstDeployment_new", false).click();
        
        
        getDynamicElement("newTagInput", true).sendKeys("abcdefghijklmnopqrstuvwxyz");
        
        getDynamicElement("newTagButton").click();
        Thread.sleep(1000); //need some time to reload
        
        System.out.println(getDynamicElement("tagList", true).getText());
        assertTrue("creation user should be present as a tag", getDynamicElement("tagList", true).getText().contains("@alice")); 
        assertTrue("template category should be present as a tag", getDynamicElement("tagList").getText().contains("deployment")); 
        assertTrue("template should be present as a tag", getDynamicElement("tagList").getText().contains("firstDeployment")); 
        assertTrue("our newly specified tag should be present",getDynamicElement("tagList").getText().contains("abcdefghijklmnopqrstuvwxyz"));
        
        getDynamicElement("addTagButton").click();
        getDynamicElement("newTagInput", true).sendKeys("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        getDynamicElement("newTagButton").click();
        Thread.sleep(1000); //need some time to reload
        String tags = getDynamicElement("tagList", true).getText();
        System.out.println(tags);
        assertTrue("our newly specified tag should be present",getDynamicElement("tagList").getText().contains("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        
        getDynamicElement("addTagButton").click();
        getDynamicElement("newTagInput", true).sendKeys("._-+@%$/boe0123456789");
        getDynamicElement("newTagButton").click();
        Thread.sleep(1000); //need some time to reload
        tags = getDynamicElement("tagList", true).getText();
        System.out.println(tags);
        assertTrue("our newly specified tag should be present",getDynamicElement("tagList").getText().contains("._-+@%$boe0123456789")); // slashes are eaten
        
    }
    
   

}
