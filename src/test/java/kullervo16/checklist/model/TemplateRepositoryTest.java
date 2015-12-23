
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
public class TemplateRepositoryTest {
    
    private TemplateRepository repository;
    
    @Before
    public void setup() {
        this.repository = new TemplateRepository();
        this.repository.loadData("./src/test/resources/data/templates");
    }
        /**
     * Test of getTemplateNames method, of class TemplateRepository.
     */
    @Test
    public void testGetTemplateNames() {
        System.out.println("getTemplateNames");
                
        List<String> result = this.repository.getTemplateNames();
        assertEquals(2, result.size());
        assertTrue(result.contains("/deployment/firstDeployment.yml"));
        assertTrue(result.contains("/development/startProject.yml"));
    }
    
    @Test
    public void testGetTemplate() {
        Template t = this.repository.getTemplate("/deployment/firstDeployment.yml");
        assertNotNull(t);
    }
}
