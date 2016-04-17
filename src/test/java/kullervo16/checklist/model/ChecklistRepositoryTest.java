/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import kullervo16.checklist.repository.ChecklistRepository;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jeve
 */
public class ChecklistRepositoryTest {
    
    private ChecklistRepository repository;
    
    @Before
    public void setup() {  
        this.repository = ChecklistRepository.INSTANCE;
        ChecklistRepository.loadData("./src/test/resources/data/checklists");
    }

    /**
     * Test of getChecklistNames method, of class ChecklistRepository.
     */
    @Test
    public void testGetChecklistNames() {
        System.out.println("getChecklistNames");
        
        List<String> result = this.repository.getChecklistNames();
        assertEquals(3, result.size());
        
        assertTrue(result.contains("deployment_20151124.yml"));
    }
    
    @Test
    public void testGetChecklist() {
        Checklist cl = this.repository.getChecklist("deployment_20151124.yml");
        assertNotNull(cl);
    }
    
    @Test
    public void testListChecklist() {
        List<ChecklistInfo> cli = this.repository.getChecklistInformation(null, null);
        assertEquals(3, cli.size());        
        assertNotNull(cli.get(0).getUuid());        
        assertNotNull(cli.get(0).getTags());        
    }
    
    @Test
    public void testListChecklistWithTag() {
        List<ChecklistInfo> cli = this.repository.getChecklistInformation("odt", null);
        assertEquals(3, cli.size());        
        assertNotNull(cli.get(0).getUuid());        
        assertNotNull(cli.get(0).getTags());     
        assertEquals(1, this.repository.getChecklistInformation("cl1", null).size());
    }
    
    @Test
    public void testListChecklistWithMilestone() {
        
        assertEquals(1, this.repository.getChecklistInformation(null, "readyForDeployment").size());        
        assertEquals(0, this.repository.getChecklistInformation(null, "nonExisting").size());   
        assertEquals(0, this.repository.getChecklistInformation(null, "deployed").size());   // not yet reached
    }
    
    @Test
    public void testListChecklistWithTagAndMilestone() {
        assertEquals(1, this.repository.getChecklistInformation("odt", "readyForDeployment").size());  
        assertEquals(0, this.repository.getChecklistInformation("cl1", "readyForDeployment").size());        
    }
    
    @Test
    public void testGetChecklistsForTemplate() {
        assertEquals(2, ((List)this.repository.getChecklistsForTemplate("/deployment/firstDeployment")).size());  
        assertEquals(1, ((List)this.repository.getChecklistsForTemplate("/deployment/firstDeployment2")).size());        
    }
    
    @Test
    public void testSorting() {
        List<ChecklistInfo> cliList = new LinkedList<>();
        cliList.add(createCLI("third", 4000));
        cliList.add(createCLI("sixth", 1000));
        cliList.add(createCLI("first", 6000));
        cliList.add(createCLI("second", 5000));   
        cliList.add(createCLI("fifth", 2000));
        cliList.add(createCLI("fourth", 3000));
        
        Collections.sort(cliList);
        assertEquals("first", cliList.get(0).getUuid());
        assertEquals("second", cliList.get(1).getUuid());
        assertEquals("third", cliList.get(2).getUuid());
        assertEquals("fourth", cliList.get(3).getUuid());
        assertEquals("fifth", cliList.get(4).getUuid());
        assertEquals("sixth", cliList.get(5).getUuid());
    }
    
    private ChecklistInfo createCLI(String uuid, long timestamp) {
        Checklist temp = new Checklist();
        temp.setId(uuid);
        Step tempStep = new Step();
        tempStep.setLastUpdate(new Date(timestamp));
        temp.setSteps(Arrays.asList(tempStep));        
        return new ChecklistInfo(temp);
    }
    
    @Test
    public void testGetTagInfo() {
        List<TagcloudEntry> tagInfo = this.repository.getTagInfo();
        assertEquals(4, tagInfo.size());
        assertEquals("openshift", tagInfo.get(0).getText());
        assertEquals(3, tagInfo.get(0).getWeight());
        assertEquals("cl1", tagInfo.get(1).getText());
        assertEquals(1, tagInfo.get(1).getWeight());
        assertEquals("odt", tagInfo.get(2).getText());
        assertEquals(3, tagInfo.get(2).getWeight());
        assertEquals("deployment", tagInfo.get(3).getText());
        assertEquals(3, tagInfo.get(3).getWeight());
    }
    
    @Test
    public void testMilestoneInfo() {
        List<TagcloudEntry> tagInfo = this.repository.getMilestoneInfo();
        assertEquals(1, tagInfo.size());
        assertEquals("readyForDeployment", tagInfo.get(0).getText());
        assertEquals(1, tagInfo.get(0).getWeight());
        
    }
}
