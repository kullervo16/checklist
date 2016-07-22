
package kullervo16.checklist.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import kullervo16.checklist.model.ErrorMessage;
import kullervo16.checklist.model.ErrorMessage.Severity;
import kullervo16.checklist.model.persist.TemplatePersister;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Test the template validation workings
 * @author jef
 */
public class ValidationTest {

    @Test
    public void testInvalid1() {
        List<ErrorMessage> errors = getErrors("./src/test/resources/validation/invalid1.yml");
        System.err.println(errors);
        assertEquals(3, errors.size());
        assertTrue(errors.get(0).getSeverity().equals(Severity.MAJOR));
        assertTrue(errors.get(1).getSeverity().equals(Severity.MAJOR));
        assertTrue(errors.get(2).getSeverity().equals(Severity.MAJOR));        
                
        assertEquals("Missing tag : description",errors.get(0).getDescription());
        assertEquals("Missing tag : steps",errors.get(1).getDescription());
        assertEquals("Wrong data in Tags",errors.get(2).getDescription());
    }
    
    @Test
    public void testInvalid2() {
        List<ErrorMessage> errors = getErrors("./src/test/resources/validation/invalid2.yml");
        System.err.println(errors);
        assertEquals(4, errors.size());
        assertTrue(errors.get(0).getSeverity().equals(Severity.MAJOR));
        assertTrue(errors.get(1).getSeverity().equals(Severity.MAJOR));
        assertTrue(errors.get(2).getSeverity().equals(Severity.MAJOR));
        assertTrue(errors.get(3).getSeverity().equals(Severity.WARNING));        
        
        assertEquals("Invalid tag value: displayName",errors.get(0).getDescription());
        assertEquals("Invalid tag value: description",errors.get(1).getDescription());        
        assertEquals("Missing tag : steps",errors.get(2).getDescription());
        assertEquals("Duplicate tag : deployment",errors.get(3).getDescription());        
    }
    
    @Test
    public void testInvalid3() {
        List<ErrorMessage> errors = getErrors("./src/test/resources/validation/invalid3.yml");
        System.err.println(errors);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getSeverity().equals(Severity.CRITICAL));        
        
        assertEquals("Invalid YAML.",errors.get(0).getDescription());
        
    }
    
    @Test
    public void testInvalid4() {
        List<ErrorMessage> errors = getErrors("./src/test/resources/validation/invalid4.yml");
        System.err.println(errors);
        assertEquals(5, errors.size());
        assertTrue(errors.get(0).getSeverity().equals(Severity.MAJOR)); 
        assertTrue(errors.get(1).getSeverity().equals(Severity.MAJOR)); 
        assertTrue(errors.get(2).getSeverity().equals(Severity.MAJOR)); 
        assertTrue(errors.get(3).getSeverity().equals(Severity.WARNING)); 
        assertTrue(errors.get(4).getSeverity().equals(Severity.MAJOR));  
        
        assertEquals("Missing tag : id",errors.get(0).getDescription());
        assertEquals("Invalid tag value: weight",errors.get(1).getDescription());
        assertEquals("Invalid tag value: weight",errors.get(2).getDescription());
        assertEquals("Referenced subchecklist /development/verifyDeployment does not (yet) exist",errors.get(3).getDescription());
        assertEquals("Duplicate id : createApplication",errors.get(4).getDescription());
        
        
        assertEquals("/steps/1/id",errors.get(0).getDetails());
        assertEquals("/steps/1/weight should be a valid positive Integer",errors.get(1).getDetails());
        assertEquals("/steps/3/weight should be a valid Integer",errors.get(2).getDetails());
        assertEquals("/steps/3 Unless you add this subchecklist, instantiation will fail at runtime.",errors.get(3).getDetails());
        assertEquals("You used the same id twice in the template",errors.get(4).getDetails());
        
    }
    
    @Test
    public void testInvalid5() {
        List<ErrorMessage> errors = getErrors("./src/test/resources/validation/invalid5.yml");
        System.err.println(errors);
        assertEquals(2, errors.size());
        assertTrue(errors.get(0).getSeverity().equals(Severity.MAJOR));  
        assertTrue(errors.get(1).getSeverity().equals(Severity.MAJOR)); 
        
        assertEquals("More than 1 of (subchecklist, question, action) specified",errors.get(0).getDescription());
        assertEquals("Subchecklist, question or action is mandatory",errors.get(1).getDescription());
        
        
        assertEquals("/steps/1 contains more than 1 of (subchecklist, question,action). Only one is allowed.",errors.get(0).getDetails());
        assertEquals("/steps/3 contains neither subchecklist, question nor action. One of the 2 is required.",errors.get(1).getDetails());
        
    }
    
     @Test
    public void testBinary() {
        List<ErrorMessage> errors = getErrors("./screenshot_choice.png");
        System.err.println(errors);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getSeverity().equals(Severity.CRITICAL));        
        
        assertEquals("Invalid YAML.",errors.get(0).getDescription());
        
    }
    
    private List<ErrorMessage> getErrors(String path) {
        try {
            return TemplatePersister.validateTemplate(IOUtils.toString(new FileInputStream(new File(path))));
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        return null;
    }
}
