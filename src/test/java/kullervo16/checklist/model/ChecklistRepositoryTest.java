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
        List<ChecklistInfo> cli = this.repository.getChecklistInformation();
        assertEquals(3, cli.size());        
        assertNotNull(cli.get(0).getUuid());        
        assertNotNull(cli.get(0).getTags());        
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
}
