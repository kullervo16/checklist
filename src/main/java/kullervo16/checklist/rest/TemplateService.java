/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kullervo16.checklist.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.ErrorMessage;
import kullervo16.checklist.model.Step;
import kullervo16.checklist.model.StepStats;
import kullervo16.checklist.model.Template;
import kullervo16.checklist.model.TemplateInfo;
import kullervo16.checklist.model.TemplateStats;
import kullervo16.checklist.repository.ChecklistRepository;
import kullervo16.checklist.repository.TemplateRepository;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * REST service to expose the templates via JSON.
 *
 * @author jef
 */
@Path("/template")
@Stateless
public class TemplateService {
    
    // use singleton repository to make sure we are all working on the same backend (@Singleton does not seem to do that job like it should)    
    TemplateRepository templateRepository = TemplateRepository.INSTANCE;
    
    
    ChecklistRepository checklistRepository = ChecklistRepository.INSTANCE;
        
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TemplateInfo> listTemplateNames() {                
        return this.templateRepository.getTemplateInformation();

    }
    
    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Template getTemplate(@QueryParam("id") String id) {        
        return this.templateRepository.getTemplate(id);    
    }
    
    @POST
    @Path("/createChecklist")
    @Produces(MediaType.TEXT_PLAIN)
    public String createChecklist(@QueryParam("id") String id, @QueryParam("parent") String parent) throws URISyntaxException {    
        Template template = this.templateRepository.getTemplate(id);
        if(template == null) {
            throw new IllegalArgumentException("Unknown template "+id);
        }
        
        return this.checklistRepository.createFromTemplate(id, template, parent);        
    }
    
    @POST
    @Path("/upload")
    @Consumes("multipart/form-data")
    public List<ErrorMessage> uploadFile(MultipartFormDataInput input, @QueryParam("name") String fileName) {



            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
            List<InputPart> inputParts = uploadForm.get("file");

            for (InputPart inputPart : inputParts) {

                               

             try (InputStream inputStream = inputPart.getBody(InputStream.class,null)) {
                 
                 return templateRepository.validateAndUpdate(fileName, inputStream);

              } catch (IOException e) {
                    e.printStackTrace();
              }

            }
            throw new IllegalArgumentException("No input parts found");

    }
    
    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * This method calculates the statistics for 1 given template (# of occurence, successrate per step, ...)
     * @param id
     * @return 
     */
    public TemplateStats getTemplateStats(@QueryParam("id") String id) {
        TemplateStats result = new TemplateStats();
        result.setId(id);
        
        Template currentTemplate = this.templateRepository.getTemplate(id);
        for(Checklist cl : this.checklistRepository.getChecklistsForTemplate(id)) {
            result.setNumberOfOccurrences(result.getNumberOfOccurrences()+1);
            for(Step step : cl.getSteps()) {                 
                
                List<StepStats> ssList;
                // The problem is that List.indexOf does not allow acustom comparator to be entered, so instead of looping... used a Lambda for it... 
                // does the same trick in a single readable (ahum) line.
                if(currentTemplate.getSteps().stream().filter(st -> st.getId().equals(step.getId())).iterator().hasNext()) {
                    // present in the current template, look in the current steps 
                    ssList = result.getCurrentStepList();
                } else {
                    // no longer present in the current template, look in the othersteps
                    ssList = result.getOtherStepList();
                }
                
                StepStats ss;
                try {
                    ss = ssList.stream().filter(walker -> walker.getName().equals(step.getId())).iterator().next();
                }catch(NoSuchElementException nse) { 
                    // new one
                    ss = new StepStats();
                    ss.setName(step.getId());
                    ssList.add(ss);
                }
                ss.update(step);
            }
        }

        return result;
    }    

}
