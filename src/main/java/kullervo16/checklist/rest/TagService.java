
package kullervo16.checklist.rest;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ejb.Stateless;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import kullervo16.checklist.model.Tagcloud;
import kullervo16.checklist.repository.ChecklistRepository;

/**
 * REST service to expose the tags via JSON.
 *
 * @author jef
 */
@Path("/tags")
@Stateless
public class TagService {

    // use singleton repository to make sure we are all working on the same backend (@Singleton does not seem to do that job like it should)    
    private final ChecklistRepository checklistRepository = ChecklistRepository.INSTANCE;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Tagcloud listTags(@QueryParam("filter") final String filter) {
        return checklistRepository.getTagInfo(filter);
    }   
    
    @DELETE   
    @Path("/{tagName}")
    public Response deleteTag(@PathParam("tagName") final String tagName, @QueryParam("newName") final String newName) throws URISyntaxException {
        
        
        if(newName != null) {
            checklistRepository.mergeTag(tagName, newName);
        } else {
            checklistRepository.deleteTag(tagName);
        }
        
        return Response.seeOther(new URI("tags")).build();        
    } 
}
