
package kullervo16.checklist.rest;

import java.util.List;
import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import kullervo16.checklist.model.TagcloudEntry;
import kullervo16.checklist.repository.ChecklistRepository;

/**
 * REST service to expose the milestones via JSON.
 *
 * @author jef
 */
@Path("/milestones")
@RequestScoped
public class MilestoneService {
    
    // use singleton repository to make sure we are all working on the same backend (@Singleton does not seem to do that job like it should)    
    private final ChecklistRepository checklistRepository = ChecklistRepository.INSTANCE;
       
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TagcloudEntry> listMilestones(@QueryParam("filter") final String filter) {
        return checklistRepository.getMilestoneInfo(filter);
    }
}
