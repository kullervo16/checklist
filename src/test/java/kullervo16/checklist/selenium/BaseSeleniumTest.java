
package kullervo16.checklist.selenium;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base class with some handy methods to be used in all kinds of tests
 * @author jef
 */
public abstract class BaseSeleniumTest {

    protected static final String BASE_URL = "http://localhost:8084/checklist/";
    protected WebDriver driver;
    protected WebDriverWait wait;
    
    @Before
    public void setUp() throws Exception {
        driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, 2);
    }

    @After
    public void tearDown() throws Exception {
        driver.close();
    }

    /**
     * Get an element by ID without waiting (so when you already have spent your initial wait)
     * @param id
     * @return 
     */
    protected WebElement getDynamicElement(String id) {
        return getDynamicElement(id, false);
    }

    /**
     * Get an element by ID and specify waiting 
     * @param id
     * @return 
     */
    protected WebElement getDynamicElement(String id, boolean executeWait) {
        if (executeWait) {
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
            } catch (TimeoutException te) {
                return null;
            }
        }
        try {
            return driver.findElement(By.id(id));
        } catch (NoSuchElementException nse) {
            return null;
        }
    }

    protected void getPage(String page) {
        driver.get(TemplateTest.BASE_URL + page);
    }

    protected void login(String user, String pwd) {
        WebElement userNameField = driver.findElement(By.id("username"));
        assertNotNull("No username field", userNameField);
        userNameField.sendKeys(user);
        WebElement passwordField = driver.findElement(By.id("password"));
        assertNotNull("No password field", passwordField);
        passwordField.sendKeys(pwd);
        WebElement login = driver.findElement(By.id("kc-login"));
        assertNotNull("No login button", login);
        login.click();
    }

    

    protected void instrumentBackend(String templateDir, String checklistDir) throws IOException {
       
        // delete current data
        FileUtils.deleteDirectory(new File(TARGETDATA+"/templates"));
        FileUtils.deleteDirectory(new File(TARGETDATA+"/checklists"));
        
        // copy requested data to the location
        File td = new File(TARGETDATA+"/templates");
        td.mkdirs();
        if(templateDir != null) {
            FileUtils.copyDirectory(new File(templateDir), td );
        }
        File cd = new File(TARGETDATA+"/checklists");
        cd.mkdirs();
        if(checklistDir != null) {
            FileUtils.copyDirectory(new File(checklistDir), cd );
        }
        
        // create a secret to be used in the reset command        
        FileWriter writer = null;
        long timestamp = System.currentTimeMillis();
       
        
        File secretfile = new File(TARGETDATA+timestamp);
        writer = new FileWriter(secretfile);
        writer.append("secret");
        writer.flush();
        writer.close();
        
        
        // execute reload
        driver.get(BASE_URL+"/reset?"+timestamp);
        assertEquals("Could not reset properly", "RESET",driver.getTitle());
        
        secretfile.delete();
        
        
    }
    private static final String TARGETDATA = "./target/data/";
}
