/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.integration;

import java.util.List;
import javax.ws.rs.client.WebTarget;
import kullervo16.checklist.model.Template;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test to verify the Template REST endpoint on a deployed server
 *
 * @author jef
 */
public class TemplateRestTest extends AbstractRestTest{
       

    @Test
    public void testList() {
        WebTarget target = client.target(REST_URL+"/template/list");
        response     = target.request().get();
        List<String> result = response.readEntity(List.class);

        assertEquals(5,result.size());
    }
    

    @Test
    public void testGet() {
        WebTarget target = client.target(REST_URL+"/template/get");
        response     = target.request().get();
        Template result = response.readEntity(Template.class);

        assertEquals("boe",result.getDisplayName());
    }
}

