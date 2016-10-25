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
        List<String> tagList = new LinkedList<>();
        tagList.add("odt");
        List<ChecklistInfo> cli = this.repository.getChecklistInformation(tagList, null);
        assertEquals(2, cli.size());        
        assertNotNull(cli.get(0).getUuid());        
        assertNotNull(cli.get(0).getTags());    
        tagList.clear();
        tagList.add("cl1");   
        assertEquals(1, this.repository.getChecklistInformation(tagList, null).size());
    }
    
    @Test
    public void testListChecklistWithMilestone() {        
        List<String> msList  = new LinkedList<>();
        msList.add("readyForDeployment");
        assertEquals(1, this.repository.getChecklistInformation(null, msList).size());  
        msList.clear();
        msList.add("nonExisting");
        assertEquals(0, this.repository.getChecklistInformation(null, msList).size());   
        msList.clear();
        msList.add("deployed");
        assertEquals(0, this.repository.getChecklistInformation(null, msList).size());   // not yet reached
    }
    
    @Test
    public void testListChecklistWithTagAndMilestone() {
        List<String> tagList = new LinkedList<>();
        List<String> msList  = new LinkedList<>();
        tagList.add("odt");
        msList.add("readyForDeployment");
        assertEquals(1, this.repository.getChecklistInformation(tagList, msList).size());  
        tagList.clear();        
        tagList.add("cl1");        
        assertEquals(0, this.repository.getChecklistInformation(tagList, msList).size());        
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
        List<TagcloudEntry> tagInfo = this.repository.getTagInfo(null);
        assertEquals(2, tagInfo.size());
        assertEquals("cl1", tagInfo.get(0).getText());
        assertEquals(1, tagInfo.get(0).getWeight());
        assertEquals("odt", tagInfo.get(1).getText());
        assertEquals(2, tagInfo.get(1).getWeight());

        // now with a filter
        tagInfo = this.repository.getTagInfo("openshift");
        assertEquals(2, tagInfo.size());
        assertEquals("cl1", tagInfo.get(0).getText());
        assertEquals(1, tagInfo.get(0).getWeight());
        assertEquals("odt", tagInfo.get(1).getText());
        assertEquals(2, tagInfo.get(1).getWeight());

        tagInfo = this.repository.getTagInfo("cl1");
        assertEquals(0, tagInfo.size());

    }
    
    @Test
    public void testMilestoneInfo() {
        List<TagcloudEntry> tagInfo = this.repository.getMilestoneInfo(null);
        assertEquals(1, tagInfo.size());
        assertEquals("readyForDeployment", tagInfo.get(0).getText());
        assertEquals(1, tagInfo.get(0).getWeight());
        
    }
}
