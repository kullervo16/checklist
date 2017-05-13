package kullervo16.checklist.selenium;

import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

/**
 * End-to-End tests based on the checklist page.
 * 
 * @author jef
 */
public class TagTest extends BaseSeleniumTest {

    @Before
    public void loadData() throws IOException {
        this.instrumentBackend( null, "./src/test/resources/data/selenium/checklists");
    }
    
    @Test
    public void testCreateWithTags() throws Exception {
        getPage("tag.html");
        assertEquals("Log in to Checklist",driver.getTitle());
        
        login("alice", "secret");
        
        assertEquals("Tags",driver.getTitle());
        
        
        assertEquals("5",getDynamicElement("weight_2.2.3", true).getText()); // first call... be patient
        assertEquals("1",getDynamicElement("weight_2.0.9").getText());
        
        getDynamicElement("edit_2.2.3").click();
        
        getDynamicElement("radio_merge",true).click();
        

        Select oSelect = new Select(driver.findElement(By.id("mergedTag")));
 
        oSelect.selectByVisibleText("2.0.9");
        
        getDynamicElement("okButton").click();
        Thread.sleep(750);
        Alert alert = driver.switchTo().alert();
        assertEquals("Are you sure you want to merge 2.2.3? This action cannot be undone...", alert.getText());
        alert.accept();
        Thread.sleep(750);
        
        assertEquals("6",getDynamicElement("weight_2.0.9", true).getText()); // first call... be patient
        assertNull(getDynamicElement("weight_2.2.3"));
        
        
        
        
    }
    
   

}
