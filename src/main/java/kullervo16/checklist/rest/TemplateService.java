
package kullervo16.checklist.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import kullervo16.checklist.model.Checklist;
import kullervo16.checklist.model.ErrorMessage;
import kullervo16.checklist.model.Step;
import kullervo16.checklist.model.StepStats;
import kullervo16.checklist.model.Template;
import kullervo16.checklist.model.TemplateInfo;
import kullervo16.checklist.model.TemplateStats;
import kullervo16.checklist.repository.ChecklistRepository;
import kullervo16.checklist.repository.TemplateRepository;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * REST service to expose the templates via JSON.
 *
 * @author jef
 */
@Path("/templates")
@Stateless
public class TemplateService {
    
    // use singleton repository to make sure we are all working on the same backend (@Singleton does not seem to do that job like it should)    
    TemplateRepository templateRepository = TemplateRepository.INSTANCE;
    
    
    ChecklistRepository checklistRepository = ChecklistRepository.INSTANCE;
        
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TemplateInfo> listTemplateNames() {                
        return this.templateRepository.getTemplateInformation();

    }
    
    @GET
    @Path("/{folder}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Template getTemplate(@PathParam("folder") String folder, @PathParam("name") String name) {        
        return this.templateRepository.getTemplate(folder, name);    
    }
    
    @GET
    @Path("/{folder}/{name}/content")
    @Produces(MediaType.TEXT_PLAIN)
    public Response downloadTemplate(@PathParam("folder") String folder, @PathParam("name") String name) {        
        Template t =this.templateRepository.getTemplate(folder, name);    
        if(t == null) {
            throw new IllegalArgumentException("Template with id "+folder+"/"+name+" not found...");
        }
        StreamingOutput stream = (OutputStream os) -> {
            try(FileInputStream fis = new FileInputStream(t.getPersister().getFile())) {
                IOUtils.copy(fis, os);
            }
        };
        return Response.ok().entity(stream).build(); 
    }
    
    
    
    @DELETE
    @Path("/{folder}/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteTemplate(@PathParam("folder") String folder, @PathParam("name") String name) throws URISyntaxException {    
        Template template = this.templateRepository.getTemplate(folder,name);
        if(template == null) {
            throw new IllegalArgumentException("Unknown template "+folder+"/"+name);
        }
        this.templateRepository.deleteTemplate(template);
        return template.getId();        
    }
    
    
    @PUT
    @Path("/{folder}/{name}")
    @Consumes("multipart/form-data")
    public List<ErrorMessage> uploadFile(MultipartFormDataInput input,@PathParam("folder") String folder, @PathParam("name") String name) {



            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
            List<InputPart> inputParts = uploadForm.get("file");

            for (InputPart inputPart : inputParts) {

                               

             try (InputStream inputStream = inputPart.getBody(InputStream.class,null)) {
                 
                 return templateRepository.validateAndUpdate("/"+folder+"/"+name, inputStream);

              } catch (IOException e) {
                    e.printStackTrace();
              }

            }
            throw new IllegalArgumentException("No input parts found");

    }
    
    @GET
    @Path("/{folder}/{name}/stats")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * This method calculates the statistics for 1 given template (# of occurence, successrate per step, ...)
     * @param id
     * @return 
     */
    public TemplateStats getTemplateStats(@PathParam("folder") String folder, @PathParam("name") String name) {
        TemplateStats result = new TemplateStats();
        
        
        Template currentTemplate = this.templateRepository.getTemplate(folder, name);
        String id = currentTemplate.getId();
        result.setId(id);
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
