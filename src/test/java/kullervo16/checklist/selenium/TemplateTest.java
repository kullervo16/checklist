package kullervo16.checklist.selenium;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author jef
 */
public class TemplateTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final static String BASE_URL = "http://localhost:8084/checklist/";

    @Before
    public void setUp() throws Exception {               
        driver = new FirefoxDriver();   
        wait = new WebDriverWait(driver, 2);
    }
    
    @After
    public void tearDown() throws Exception {
        driver.close();
    }
    
    private void getPage(String page) {
        driver.get(BASE_URL+page);
    }
    
    private void login(String user, String pwd) {
        WebElement userNameField = driver.findElement(By.id("username"));
        assertNotNull("No username field",userNameField);
        userNameField.sendKeys(user);
        WebElement passwordField = driver.findElement(By.id("password"));
        assertNotNull("No password field",passwordField);
        passwordField.sendKeys(pwd);
        WebElement login = driver.findElement(By.id("kc-login"));
        assertNotNull("No login button", login);
        login.click();
    }
    
    private WebElement getDynamicElement(String id) {
        return getDynamicElement(id, true);
    }
    
    private WebElement getDynamicElement(String id, boolean executeWait) {
        if(executeWait) {
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
            }catch(TimeoutException te) {
                return null;
            }
        }
        try {
            return driver.findElement(By.id(id));
        }catch(NoSuchElementException nse) {
            return null;
        }
    }
    
    @Test
    public void testLoadAdmin() throws Exception {
        getPage("templates.html");
        assertEquals("Log in to Checklist",driver.getTitle());
        
        login("alice", "secret");
        
        assertEquals("Templates",driver.getTitle());

        assertNotNull(getDynamicElement("/deployment/deployment_delete"));
        assertNotNull(getDynamicElement("/deployment/deployment_upload"));
        assertNotNull(getDynamicElement("/deployment/deployment_download"));
    }
    
    @Test
    public void testLoadModify() throws Exception {
        getPage("templates.html");
        assertEquals("Log in to Checklist",driver.getTitle());
        
        login("marc", "secret");
        
        assertEquals("Templates",driver.getTitle());
        
        assertNull(getDynamicElement("/deployment/deployment_delete"));
        assertNull(getDynamicElement("/deployment/deployment_upload", false)); // already waited
        assertNull(getDynamicElement("/deployment/deployment_download", false));
    }
}
