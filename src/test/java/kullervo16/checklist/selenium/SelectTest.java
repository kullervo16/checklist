package kullervo16.checklist.selenium;

import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * End-to-End tests based on the select page.
 * 
 * @author jef
 */
public class SelectTest extends BaseSeleniumTest {

    @Before
    public void loadData() throws IOException {
        this.instrumentBackend( null, "./src/test/resources/data/selenium/checklists");
    }
    
    @Test
    public void testTagSelectionWithFilter() throws Exception {
        getPage("select.html");
        assertEquals("Log in to Checklist",driver.getTitle());
        
        login("alice", "secret");
        
        assertEquals("Tags & milestones",driver.getTitle());
        
        Thread.sleep(1000); //tagcloud takes some time to display
        assertTrue(getDynamicElement("tags", true).getText().contains("1.0.7"));        
        assertTrue(getDynamicElement("tags", true).getText().contains("2.2.3"));
        assertTrue(getDynamicElement("tags", true).getText().contains("2.0.11"));
        getDynamicElement("filter", true).sendKeys("2"); // filtering is local, so directly
        assertFalse(getDynamicElement("tags", true).getText().contains("1.0.7"));
        assertTrue(getDynamicElement("tags", true).getText().contains("2.2.3"));
        assertTrue(getDynamicElement("tags", true).getText().contains("2.0.11"));
        
        // now click on 2.0.11
       getDynamicElement("tags_word_2").click();
       Thread.sleep(1000); //tagcloud takes some time to display
       assertEquals("prd\nacceptance", getDynamicElement("tags").getText()); // assert only 2 tags left
       
       // now click on acceptance
       getDynamicElement("tags_word_1").click();
       Thread.sleep(1000); // will be moving to selection and then to checklist since only 1 instance
       assertEquals("Checklist", driver.getTitle()); 
       assertTrue(driver.getCurrentUrl().contains("252ec25f-8996-4abb-924c-df667c611ec7"));
    }
    
    @Test
    public void testMilestoneSelection() throws Exception {
        getPage("select.html");
        assertEquals("Log in to Checklist",driver.getTitle());
        
        login("alice", "secret");
        
        assertEquals("Tags & milestones",driver.getTitle());
        
        Thread.sleep(1000); //tagcloud takes some time to display
        assertTrue(getDynamicElement("milestones", true).getText().contains("verified"));    
        assertTrue(getDynamicElement("milestones", true).getText().contains("deployed")); 

        // now click on verified
        getDynamicElement("milestones_word_3").click();
        Thread.sleep(1000); //tagcloud takes some time to display
        assertEquals("deployed", getDynamicElement("milestones").getText()); // assert only 1 tag left
       
        // now clear selection
        getDynamicElement("milestone_clear").click();
        Thread.sleep(1000); //tagcloud takes some time to display
        assertTrue(getDynamicElement("milestones", true).getText().contains("verified"));    
        assertTrue(getDynamicElement("milestones", true).getText().contains("deployed")); 
        
        // now click on deployed
        getDynamicElement("milestones_word_0").click();
        Thread.sleep(1000); //tagcloud takes some time to display
        // now click on verified
        getDynamicElement("milestones_word_0").click();
        Thread.sleep(1000); //tagcloud takes some time to display
      

        // now click goto -> end up in the overview screen
        getDynamicElement("milestone_goto").click();
        Thread.sleep(500);
        
        assertEquals("Checklist overview", driver.getTitle()); 
        assertTrue(driver.getCurrentUrl().contains("milestones=deployed,verified"));
    }

}
